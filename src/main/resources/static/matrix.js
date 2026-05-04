// --- DOM セットアップ ---
const wrapper = document.createElement('div');
wrapper.id = 'matrix-bg';
document.body.appendChild(wrapper);

const canvas = document.createElement('canvas');
Object.assign(canvas.style, {
  position: 'fixed', top: '0', left: '0',
  width: '100%', height: '100%',
  zIndex: '-1', pointerEvents: 'none',
});
wrapper.appendChild(canvas);

const ctx = canvas.getContext('2d');

// --- 定数 ---
const FONT_SIZE = 16;
const FONT_FACE = 'bold ' + FONT_SIZE + 'px "MS Gothic"';
const CHARS = 'ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝABCDEFGHIJKLMNOPQRSTUVWXYZ+*-/%&!#$0123456789';

// 各段の [fillColor, shadowBlur] ペア (index 0 = 先頭)
const TRAIL = [
  [['#00ff00', 40], ['#00ff00', 25], ['#88ff88', 8]],
  [['#00ff00', 20], ['#00ff00', 10]],
  [['#00cc00', 10]],
  [['#009900',  5]],
];

// --- 状態変数 ---
let colWidth, drops, flips, speeds, intervalId;

// --- 描画ユーティリティ ---
function charForPos(col, pos) {
  return CHARS[((col * 137 + pos * 31) >>> 0) % CHARS.length];
}

function drawChar(char, x, y, flip) {
  if (flip) {
    ctx.setTransform(-1, 0, 0, 1, x + colWidth, 0);
    ctx.fillText(char, 0, y, colWidth);
    ctx.setTransform(1, 0, 0, 1, 0, 0);
  } else {
    ctx.fillText(char, x, y, colWidth);
  }
}

function drawGlyph(char, x, y, flip, passes) {
  ctx.shadowColor = '#00ff00';
  for (const [color, blur] of passes) {
    ctx.fillStyle = color;
    ctx.shadowBlur = blur;
    drawChar(char, x, y, flip);
  }
  ctx.shadowBlur = 0;
}
	
// --- リサイズ ---
function resize() {
  canvas.width  = window.innerWidth;
  canvas.height = window.innerHeight;
  ctx.font = FONT_FACE;
  colWidth = Math.ceil(ctx.measureText('ｱ').width);
  ctx.fillStyle = '#000';
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  const cols = Math.floor(canvas.width  / colWidth);
  const rows = Math.floor(canvas.height / FONT_SIZE);
  drops  = Array.from({length: cols}, () => -Math.floor(Math.random() * rows));
  flips  = Array.from({length: cols}, () => Math.random() < 0.3);
  speeds = Array.from({length: cols}, () => 0.5 + Math.random());
}

// --- メインループ ---
function draw() {
  ctx.fillStyle = 'rgba(0, 0, 0, 0.07)';
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.font = FONT_FACE;
  ctx.textBaseline = 'top';
  ctx.textAlign = 'left';

  for (let i = 0; i < drops.length; i++) {
    const pos  = Math.floor(drops[i]);
    const x    = i * colWidth;
    const flip = flips[i];

    for (let d = 0; d < TRAIL.length; d++) {
      if (pos >= d) drawGlyph(charForPos(i, pos - d), x, (pos - d) * FONT_SIZE, flip, TRAIL[d]);
    }

    if (pos * FONT_SIZE > canvas.height && Math.random() > 0.975) {
      drops[i]  = 0;
      flips[i]  = Math.random() < 0.3;
      speeds[i] = 0.5 + Math.random();
    }
    drops[i] += speeds[i];
  }
}

// --- 開始 / 停止 ---
function start() {
  if (intervalId) return;
  canvas.style.display = '';
  document.body.style.background = 'transparent';
  document.documentElement.style.background = '#000';
  intervalId = setInterval(draw, 100);
}

function stop() {
  clearInterval(intervalId);
  intervalId = null;
  canvas.style.display = 'none';
  document.documentElement.style.background = '';
  document.body.style.background = '';
}

function toggle() {
  if (intervalId) { localStorage.setItem('matrixBg', 'false'); stop();  return false; }
  else            { localStorage.setItem('matrixBg', 'true');  start(); return true;  }
}

// --- トグルボタン (全ページ共通、イベント委譲) ---
document.addEventListener('click', (e) => {
  const btn = e.target.closest('#matrix-toggle');
  if (!btn) return;
  btn.textContent = '背景: ' + (toggle() ? 'ON' : 'OFF');
});

document.addEventListener('turbo:load', () => {
  const btn = document.getElementById('matrix-toggle');
  if (btn) btn.textContent = '背景: ' + (intervalId ? 'ON' : 'OFF');
});

// --- Turbo: ナビゲーション前に wrapper を新 body へ移動 ---
document.addEventListener('turbo:before-render', (e) => {
  if (intervalId) e.detail.newBody.style.background = 'transparent';
  e.detail.newBody.appendChild(wrapper);
});

// --- 初期化 ---
resize();
window.addEventListener('resize', resize);
if (localStorage.getItem('matrixBg') !== 'false') start(); // 起動
