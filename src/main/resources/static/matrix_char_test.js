(function () {
  document.documentElement.style.background = '#000';
  document.body.style.background = 'transparent';

  const wrap = document.createElement('div');
  Object.assign(wrap.style, {
    position: 'fixed', top: '20px', left: '20px',
    display: 'flex', gap: '24px', alignItems: 'center',
    zIndex: '9999',
  });

  const styles = [
    { label: 'A: text-shadow 1層',      color: '#00ff00', textShadow: '0 0 12px #00ff00' },
    { label: 'B: text-shadow 3層',      color: '#00ff00', textShadow: '0 0 6px #00ff00,0 0 14px #00ff00,0 0 28px #00ff00' },
    { label: 'C: 白文字+緑shadow',      color: '#ffffff', textShadow: '0 0 8px #00ff00,0 0 20px #00ff00' },
    { label: 'D: drop-shadow 2重',      color: '#00ff00', filter: 'drop-shadow(0 0 6px #00ff00) drop-shadow(0 0 12px #00ff00)' },
    { label: 'E: drop-shadow 3重',      color: '#00ff00', filter: 'drop-shadow(0 0 4px #0f0) drop-shadow(0 0 8px #0f0) drop-shadow(0 0 16px #0f0)' },
  ];

  'アイウエオ'.split('').forEach((c, i) => {
    const span = document.createElement('span');
    span.textContent = c;
    span.title = styles[i].label;
    Object.assign(span.style, {
      font: '48px "MS Gothic"',
      color: styles[i].color,
      textShadow: styles[i].textShadow || '',
      filter: styles[i].filter || '',
    });
    wrap.appendChild(span);
  });

  // オ（glow）＋ウ（白）の組み合わせ候補
  const combos = [
    { label: 'カ: 白+drop3重(小)',  color: '#fff', filter: 'drop-shadow(0 0 2px #0f0) drop-shadow(0 0 6px #0f0) drop-shadow(0 0 12px #0f0)' },
    { label: 'キ: 白+drop3重(中)',  color: '#fff', filter: 'drop-shadow(0 0 4px #0f0) drop-shadow(0 0 8px #0f0) drop-shadow(0 0 16px #0f0)' },
    { label: 'ク: 白+drop3重(大)',  color: '#fff', filter: 'drop-shadow(0 0 6px #0f0) drop-shadow(0 0 14px #0f0) drop-shadow(0 0 28px #0f0)' },
    { label: 'ケ: 白+text+drop',   color: '#fff', textShadow: '0 0 8px #0f0', filter: 'drop-shadow(0 0 8px #0f0) drop-shadow(0 0 16px #0f0)' },
  ];

  const wrap2 = document.createElement('div');
  Object.assign(wrap2.style, {
    position: 'fixed', top: '90px', left: '20px',
    display: 'flex', gap: '24px', alignItems: 'center',
    zIndex: '9999',
  });

  'カキクケ'.split('').forEach((c, i) => {
    const span = document.createElement('span');
    span.textContent = c;
    span.title = combos[i].label;
    Object.assign(span.style, {
      font: '48px "MS Gothic"',
      color: combos[i].color,
      textShadow: combos[i].textShadow || '',
      filter: combos[i].filter || '',
    });
    wrap2.appendChild(span);
  });

  document.body.appendChild(wrap2);

  document.body.appendChild(wrap);
})();
