(function () {
  // Turbo Drive ナビゲーション時: body の background だけ再設定して終了
  if (document.getElementById('matrix-bg')) {
    if (window.matrixBg && window.matrixBg.isEnabled()) {
      document.body.style.background = 'transparent';
    }
    return;
  }

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
  const fontSize = 16;
  const fontFace = 'bold ' + fontSize + 'px "MS Gothic"';
  const kataChars = 'ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝABCDEFGHIJKLMNOPQRSTUVWXYZ+*-/%&!#$0123456789';

  function charForPos(col, pos) {
    return kataChars[((col * 137 + pos * 31) >>> 0) % kataChars.length];
  }

  let colWidth, drops, flips, speeds;

  function resize() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    ctx.font = fontFace;
    colWidth = Math.ceil(ctx.measureText('ｱ').width);
    ctx.fillStyle = '#000';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    const cols = Math.floor(canvas.width / colWidth);
    const rows = Math.floor(canvas.height / fontSize);
    drops = Array.from({length: cols}, () => -Math.floor(Math.random() * rows));
    flips = Array.from({length: cols}, () => Math.random() < 0.3);
    speeds = Array.from({length: cols}, () => 0.5 + Math.random());
  }
  window.addEventListener('resize', resize);
  resize();

  function drawChar(char, x, y) {
    const flip = arguments[3];
    if (flip) {
      ctx.setTransform(-1, 0, 0, 1, x + colWidth, 0);
      ctx.fillText(char, 0, y, colWidth);
      ctx.setTransform(1, 0, 0, 1, 0, 0);
    } else {
      ctx.fillText(char, x, y, colWidth);
    }
  }

  function drawHead(char, x, y, flip) {
    ctx.shadowColor = '#00ff00';
    ctx.fillStyle = '#00ff00';
    ctx.shadowBlur = 40; drawChar(char, x, y, flip);
    ctx.shadowBlur = 25; drawChar(char, x, y, flip);
    ctx.fillStyle = '#88ff88';
    ctx.shadowBlur = 8;  drawChar(char, x, y, flip);
    ctx.shadowBlur = 0;
  }

  function drawTrail1(char, x, y, flip) {
    ctx.fillStyle = '#00ff00';
    ctx.shadowColor = '#00ff00';
    ctx.shadowBlur = 20; drawChar(char, x, y, flip);
    ctx.shadowBlur = 10; drawChar(char, x, y, flip);
    ctx.shadowBlur = 0;
  }

  function drawTrail2(char, x, y, flip) {
    ctx.fillStyle = '#00cc00';
    ctx.shadowColor = '#00ff00';
    ctx.shadowBlur = 10; drawChar(char, x, y, flip);
    ctx.shadowBlur = 0;
  }

  function drawTrail3(char, x, y, flip) {
    ctx.fillStyle = '#009900';
    ctx.shadowColor = '#00ff00';
    ctx.shadowBlur = 5;  drawChar(char, x, y, flip);
    ctx.shadowBlur = 0;
  }

  function draw() {
    ctx.fillStyle = 'rgba(0, 0, 0, 0.07)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.font = fontFace;
    ctx.textBaseline = 'top';
    ctx.textAlign = 'left';

    for (let i = 0; i < drops.length; i++) {
      const pos = Math.floor(drops[i]);
      const x = i * colWidth;
      const flip = flips[i];

      drawHead(charForPos(i, pos),     x, pos * fontSize,       flip);
      if (pos >= 1) drawTrail1(charForPos(i, pos - 1), x, (pos - 1) * fontSize, flip);
      if (pos >= 2) drawTrail2(charForPos(i, pos - 2), x, (pos - 2) * fontSize, flip);
      if (pos >= 3) drawTrail3(charForPos(i, pos - 3), x, (pos - 3) * fontSize, flip);

      if (pos * fontSize > canvas.height && Math.random() > 0.975) {
        drops[i] = 0;
        flips[i] = Math.random() < 0.3;
        speeds[i] = 0.5 + Math.random();
      }
      drops[i] += speeds[i];
    }
  }

  let intervalId = null;

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

  if (localStorage.getItem('matrixBg') !== 'false') {
    start();
  }

  window.matrixBg = {
    toggle() {
      if (intervalId) { localStorage.setItem('matrixBg', 'false'); stop(); return false; }
      else            { localStorage.setItem('matrixBg', 'true');  start(); return true; }
    },
    isEnabled() { return !!intervalId; },
  };

  // Turbo ナビゲーション前に wrapper を新 body へ移動して canvas を生かし続ける
  document.addEventListener('turbo:before-render', (e) => {
    e.detail.newBody.style.background = 'transparent';
    e.detail.newBody.appendChild(wrapper);
  });
})();
