/**
 * 全站公共实时信息栏：实时时钟 + 天气
 * 天气数据来源：Open-Meteo（免费、无需 API Key、支持 CORS）
 * 在页面 <body> 末尾引入：<script src="js/site-common.js"></script>
 */
(function () {
  // ---------- 样式 ----------
  var style = document.createElement('style');
  style.textContent = [
    '#siteBar{display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;',
    '  padding:8px 18px;background:#1d2b3a;color:#e8eef5;font-size:13px;font-family:"Microsoft YaHei",sans-serif;}',
    '#siteBar .seg{margin-right:14px;white-space:nowrap;}',
    '#siteBar select{background:#2d4159;color:#e8eef5;border:1px solid #40608a;border-radius:6px;',
    '  padding:2px 6px;font-size:13px;outline:none;cursor:pointer;}',
    '#siteBar a{color:#9ecbff;text-decoration:none;}'
  ].join('');
  document.head.appendChild(style);

  // ---------- 结构 ----------
  var bar = document.createElement('div');
  bar.id = 'siteBar';
  bar.innerHTML =
    '<div>' +
    '  <span class="seg" id="barClock">📅 加载中…</span>' +
    '  <span class="seg" id="barWeather">🌤 天气加载中…</span>' +
    '</div>' +
    '<div>' +
    '  <span class="seg">城市：<select id="barCity"></select></span>' +
    '  <span class="seg"><a href="/index.html">🏠 系统首页</a></span>' +
    '</div>';
  document.body.insertBefore(bar, document.body.firstChild);

  // ---------- 城市列表（默认郑州：华北水利水电大学所在地） ----------
  var CITIES = ['郑州', '北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安'];
  var sel = document.getElementById('barCity');
  CITIES.forEach(function (c) {
    var o = document.createElement('option');
    o.value = c; o.textContent = c;
    sel.appendChild(o);
  });
  sel.value = localStorage.getItem('siteBarCity') || '郑州';

  // ---------- 实时时钟 ----------
  var WEEK = ['日', '一', '二', '三', '四', '五', '六'];
  function pad(n) { return n < 10 ? '0' + n : n; }
  function tickClock() {
    var d = new Date();
    document.getElementById('barClock').textContent =
      '📅 ' + d.getFullYear() + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日' +
      ' 星期' + WEEK[d.getDay()] +
      ' ⏰ ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
  }
  tickClock();
  setInterval(tickClock, 1000);

  // ---------- WMO 天气代码 → 中文描述 ----------
  var WMO = {
    0: ['☀️', '晴'], 1: ['🌤', '基本晴'], 2: ['⛅', '多云'], 3: ['☁️', '阴'],
    45: ['🌫', '雾'], 48: ['🌫', '雾凇'],
    51: ['🌦', '小毛毛雨'], 53: ['🌦', '毛毛雨'], 55: ['🌧', '大毛毛雨'],
    56: ['🌧', '冻毛毛雨'], 57: ['🌧', '强冻毛毛雨'],
    61: ['🌦', '小雨'], 63: ['🌧', '中雨'], 65: ['🌧', '大雨'],
    66: ['🌧', '冻雨'], 67: ['🌧', '强冻雨'],
    71: ['🌨', '小雪'], 73: ['🌨', '中雪'], 75: ['❄️', '大雪'], 77: ['❄️', '雪粒'],
    80: ['🌦', '小阵雨'], 81: ['🌧', '阵雨'], 82: ['⛈', '强阵雨'],
    85: ['🌨', '小阵雪'], 86: ['❄️', '大阵雪'],
    95: ['⛈', '雷阵雨'], 96: ['⛈', '雷阵雨伴冰雹'], 99: ['⛈', '强雷雨伴冰雹']
  };

  // ---------- 天气查询（Open-Meteo，无需 Key） ----------
  var weatherTimer = null;
  function loadWeather(city) {
    var el = document.getElementById('barWeather');
    el.textContent = '🌤 ' + city + ' 天气加载中…';
    // 1. 城市 -> 经纬度
    fetch('https://geocoding-api.open-meteo.com/v1/search?name=' +
        encodeURIComponent(city) + '&count=1&language=zh&format=json')
      .then(function (r) { return r.json(); })
      .then(function (g) {
        if (!g.results || !g.results.length) throw new Error('未找到城市');
        var loc = g.results[0];
        // 2. 经纬度 -> 实时天气
        return fetch('https://api.open-meteo.com/v1/forecast?latitude=' + loc.latitude +
            '&longitude=' + loc.longitude +
            '&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto')
          .then(function (r) { return r.json(); })
          .then(function (w) { return { loc: loc, w: w.current }; });
      })
      .then(function (data) {
        var code = WMO[data.w.weather_code] || ['🌈', '未知'];
        el.textContent = code[0] + ' ' + data.loc.name + ' ' + code[1] +
          ' ' + data.w.temperature_2m + '°C' +
          ' · 湿度 ' + data.w.relative_humidity_2m + '%' +
          ' · 风速 ' + data.w.wind_speed_10m + 'km/h';
        el.title = '数据来源：Open-Meteo，每 10 分钟自动刷新';
      })
      .catch(function () {
        el.textContent = '⚠️ ' + city + ' 天气获取失败（检查网络），稍后自动重试';
      });
  }
  function refreshWeather() { loadWeather(sel.value); }

  sel.addEventListener('change', function () {
    localStorage.setItem('siteBarCity', sel.value);
    refreshWeather();
  });

  refreshWeather();
  // 每 10 分钟自动刷新天气
  weatherTimer = setInterval(refreshWeather, 10 * 60 * 1000);
})();
