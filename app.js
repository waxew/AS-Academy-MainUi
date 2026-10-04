const content = document.querySelector('#content');
const drawer = document.querySelector('#drawer');
const scrim = document.querySelector('#scrim');
const navButton = document.querySelector('#navButton');
const pageTitle = document.querySelector('#pageTitle');
const snapshotText = document.querySelector('#snapshotText');
const aboutDialog = document.querySelector('#aboutDialog');
const installButton = document.querySelector('#installButton');

const state = {
  index: null,
  course: null,
  entry: null,
  query: '',
  section: 'all',
  bookmarksOnly: false,
  deferredInstall: null,
};

const SECTION_LABELS = {
  lessons: 'درس‌ها', exercises: 'تمرین‌ها', quizzes: 'آزمون‌ها', projects: 'پروژه‌ها',
  glossary: 'واژه‌نامه', labs: 'آزمایشگاه', levels: 'سطح‌ها', chapters: 'فصل‌ها',
  references: 'منابع', course: 'اطلاعات دوره', other: 'سایر',
};

const esc = (value = '') => String(value)
  .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;').replaceAll("'", '&#039;');

function bookmarks() {
  try { return JSON.parse(localStorage.getItem('as-academy-bookmarks') || '[]'); }
  catch { return []; }
}
function isBookmarked(entry) { return bookmarks().includes(`${entry.courseSlug}:${entry.path}`); }
function toggleBookmark(entry) {
  const key = `${entry.courseSlug}:${entry.path}`;
  const set = new Set(bookmarks());
  set.has(key) ? set.delete(key) : set.add(key);
  localStorage.setItem('as-academy-bookmarks', JSON.stringify([...set]));
  render();
}

function openDrawer() { drawer.classList.add('open'); scrim.hidden = false; }
function closeDrawer() { drawer.classList.remove('open'); scrim.hidden = true; }
function resetHome() {
  state.course = null; state.entry = null; state.query = ''; state.section = 'all'; state.bookmarksOnly = false;
  history.pushState({}, '', location.pathname); render(); closeDrawer();
}

navButton.addEventListener('click', () => {
  if (state.entry) { state.entry = null; render(); return; }
  if (state.course) { state.course = null; render(); return; }
  openDrawer();
});
scrim.addEventListener('click', closeDrawer);

document.addEventListener('click', async (event) => {
  const action = event.target.closest('[data-action]')?.dataset.action;
  if (!action) return;
  if (action === 'home') resetHome();
  if (action === 'search') { resetHome(); requestAnimationFrame(() => document.querySelector('#globalSearch')?.focus()); }
  if (action === 'bookmarks') { state.course = null; state.entry = null; state.bookmarksOnly = true; render(); closeDrawer(); }
  if (action === 'theme') {
    const dark = document.documentElement.dataset.theme === 'dark';
    document.documentElement.dataset.theme = dark ? 'light' : 'dark';
    localStorage.setItem('as-academy-theme', dark ? 'light' : 'dark');
  }
  if (action === 'share') {
    const data = { title: 'AS Academy MainCourse', text: 'نسخه وب یکپارچه AS Academy', url: location.href };
    if (navigator.share) await navigator.share(data).catch(() => {});
    else await navigator.clipboard?.writeText(location.href);
    closeDrawer();
  }
  if (action === 'about') { aboutDialog.showModal(); closeDrawer(); }
  if (action === 'close-about') aboutDialog.close();
});

window.addEventListener('popstate', () => {
  if (state.entry) state.entry = null;
  else if (state.course) state.course = null;
  render();
});

window.addEventListener('beforeinstallprompt', (event) => {
  event.preventDefault(); state.deferredInstall = event; installButton.hidden = false;
});
installButton.addEventListener('click', async () => {
  if (!state.deferredInstall) return;
  state.deferredInstall.prompt();
  await state.deferredInstall.userChoice;
  state.deferredInstall = null; installButton.hidden = true;
});

function courseCard(course) {
  return `<article class="card" data-course="${esc(course.slug)}" tabindex="0">
    <div class="card-meta"><span class="badge">${esc(course.version || 'بدون نسخه')}</span><span class="badge">${course.files.length} محتوا</span></div>
    <h3>${esc(course.titleFa || course.slug)}</h3>
    <p>${esc(course.titleEn || course.slug)}</p>
    <div class="card-meta">${Object.entries(course.counts || {}).filter(([,v]) => v).slice(0,5).map(([k,v]) => `<span>${esc(SECTION_LABELS[k] || k)}: ${v}</span>`).join('')}</div>
  </article>`;
}

function entryCard(entry, showCourse = false) {
  return `<article class="card" data-entry="${esc(entry.courseSlug)}|${esc(entry.path)}" tabindex="0">
    <div class="card-meta"><span class="badge">${esc(SECTION_LABELS[entry.section] || entry.section)}</span>${isBookmarked(entry) ? '<span class="badge">★ نشان‌شده</span>' : ''}</div>
    <h3>${esc(entry.title || entry.path)}</h3>
    <p>${esc(entry.summary || entry.path)}</p>
    ${showCourse ? `<div class="card-meta"><span>${esc(entry.courseTitle || entry.courseSlug)}</span></div>` : ''}
  </article>`;
}

function filters(entries) {
  const sections = [...new Set(entries.map(x => x.section))].sort();
  return `<div class="chips"><button class="chip ${state.section === 'all' ? 'active' : ''}" data-section="all">همه</button>${sections.map(section => `<button class="chip ${state.section === section ? 'active' : ''}" data-section="${esc(section)}">${esc(SECTION_LABELS[section] || section)}</button>`).join('')}</div>`;
}

function dashboard() {
  const courses = state.index.courses;
  const allEntries = courses.flatMap(course => course.files.map(file => ({ ...file, courseSlug: course.slug, courseTitle: course.titleFa })));
  const query = state.query.trim().toLocaleLowerCase('fa');
  const marked = new Set(bookmarks());
  const results = allEntries.filter(entry => {
    if (state.bookmarksOnly && !marked.has(`${entry.courseSlug}:${entry.path}`)) return false;
    if (state.section !== 'all' && entry.section !== state.section) return false;
    if (!query) return state.bookmarksOnly;
    return (entry.searchText || `${entry.title} ${entry.summary} ${entry.path}`).toLocaleLowerCase('fa').includes(query);
  }).slice(0, 500);

  pageTitle.textContent = state.bookmarksOnly ? 'نشان‌شده‌ها' : 'AS Academy MainCourse';
  navButton.textContent = '☰';
  return `<section class="hero">
    <h1>نمای یکپارچه همه آموزش‌ها</h1>
    <p>دوره‌ها، درس‌ها، تمرین‌ها، آزمون‌ها، پروژه‌ها و واژه‌نامه AS Academy در یک نسخه وب Responsive و نصب‌پذیر.</p>
    <div class="stats"><span class="stat">${courses.length} دوره</span><span class="stat">${allEntries.length} فایل آموزشی</span><span class="stat">Offline/PWA</span></div>
  </section>
  <div class="toolbar"><input id="globalSearch" class="search-input" value="${esc(state.query)}" placeholder="جست‌وجو در همه دوره‌ها و محتواها…" aria-label="جست‌وجوی سراسری" /><span class="stat">${state.bookmarksOnly ? `${results.length} نشان‌شده` : (query ? `${results.length} نتیجه` : 'جست‌وجوی سراسری')}</span></div>
  ${filters(allEntries)}
  ${query || state.bookmarksOnly ? `<div class="section-title"><h2>${state.bookmarksOnly ? 'محتوای نشان‌شده' : 'نتایج جست‌وجو'}</h2><span>حداکثر ۵۰۰ نتیجه</span></div><div class="grid">${results.map(x => entryCard(x,true)).join('') || '<div class="empty">نتیجه‌ای پیدا نشد.</div>'}</div>` : `<div class="section-title"><h2>دوره‌ها</h2><span>انتخاب دوره برای مشاهده محتوا</span></div><div class="grid">${courses.map(courseCard).join('')}</div>`}`;
}

function courseView(course) {
  const query = state.query.trim().toLocaleLowerCase('fa');
  const entries = course.files.filter(entry => (state.section === 'all' || entry.section === state.section) && (!query || (entry.searchText || `${entry.title} ${entry.path}`).toLocaleLowerCase('fa').includes(query)));
  pageTitle.textContent = course.titleFa || course.slug;
  navButton.textContent = '←';
  return `<section class="hero"><h1>${esc(course.titleFa || course.slug)}</h1><p>${esc(course.titleEn || '')}</p><div class="stats"><span class="stat">نسخه ${esc(course.version || '-')}</span><span class="stat">${course.files.length} محتوا</span></div></section>
    <div class="toolbar"><input id="globalSearch" class="search-input" value="${esc(state.query)}" placeholder="جست‌وجو داخل این دوره…" /><span class="stat">${entries.length} مورد</span></div>
    ${filters(course.files)}
    <div class="section-title"><h2>محتوای دوره</h2><span>${esc(course.slug)}</span></div><div class="grid">${entries.map(x => entryCard({ ...x, courseSlug: course.slug })).join('') || '<div class="empty">محتوایی با این فیلتر پیدا نشد.</div>'}</div>`;
}

function mdToHtml(text) {
  return esc(text).split('\n').map(line => {
    if (line.startsWith('### ')) return `<h3>${line.slice(4)}</h3>`;
    if (line.startsWith('## ')) return `<h2>${line.slice(3)}</h2>`;
    if (line.startsWith('# ')) return `<h1>${line.slice(2)}</h1>`;
    if (line.startsWith('- ')) return `<div>• ${line.slice(2)}</div>`;
    if (!line.trim()) return '<br />';
    return `<div>${line}</div>`;
  }).join('');
}

function renderJson(value, depth = 0) {
  if (value === null || value === undefined) return '';
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') return `<div>${esc(value)}</div>`;
  if (Array.isArray(value)) return value.map((item, i) => `<section style="margin:${depth ? '10px 0' : '16px 0'};padding:${depth ? '10px' : '14px'};border:${depth ? '1px solid var(--line)' : '0'};border-radius:14px">${renderJson(item, depth + 1)}</section>`).join('');
  const title = value.title || value.titleFa || value.name || value.term || value.question || null;
  const rows = Object.entries(value).filter(([key]) => !['title','titleFa','name'].includes(key)).map(([key,val]) => {
    if (val === null || val === '' || (Array.isArray(val) && !val.length)) return '';
    const label = key === 'content' ? '' : `<strong style="display:block;margin-top:8px;color:var(--muted);font-size:.82rem">${esc(key)}</strong>`;
    return `${label}${renderJson(val, depth + 1)}`;
  }).join('');
  return `${title ? `<h${Math.min(4, depth + 2)}>${esc(title)}</h${Math.min(4, depth + 2)}>` : ''}${rows}`;
}

async function reader(entry) {
  pageTitle.textContent = entry.title || entry.path;
  navButton.textContent = '←';
  content.innerHTML = '<div class="loading-card">در حال خواندن محتوا…</div>';
  try {
    const url = `data/courses/${encodeURIComponent(entry.courseSlug)}/${entry.path.split('/').map(encodeURIComponent).join('/')}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const text = await response.text();
    let body;
    if (entry.path.endsWith('.json')) {
      try { body = renderJson(JSON.parse(text)); }
      catch { body = `<pre>${esc(text)}</pre>`; }
    } else if (entry.path.endsWith('.md')) body = mdToHtml(text);
    else body = `<pre>${esc(text)}</pre>`;
    content.innerHTML = `<article class="reader"><div class="reader-head"><div><div class="card-meta"><span class="badge">${esc(SECTION_LABELS[entry.section] || entry.section)}</span><span class="badge">${esc(entry.courseTitle || entry.courseSlug)}</span></div><h1>${esc(entry.title || entry.path)}</h1><p class="muted">${esc(entry.path)}</p></div><button id="bookmarkButton" class="bookmark-button">${isBookmarked(entry) ? '★ حذف نشان' : '☆ نشان کردن'}</button></div><div class="reader-body">${body}</div></article>`;
    document.querySelector('#bookmarkButton')?.addEventListener('click', () => toggleBookmark(entry));
  } catch (error) {
    content.innerHTML = `<div class="error-card">خواندن محتوا ناموفق بود: ${esc(error.message)}</div>`;
  }
}

function bindCards() {
  document.querySelectorAll('[data-course]').forEach(node => node.addEventListener('click', () => {
    state.course = state.index.courses.find(x => x.slug === node.dataset.course); state.entry = null; state.query = ''; state.section = 'all';
    history.pushState({}, '', `#course=${encodeURIComponent(state.course.slug)}`); render();
  }));
  document.querySelectorAll('[data-entry]').forEach(node => node.addEventListener('click', () => {
    const [slug, path] = node.dataset.entry.split('|');
    const course = state.index.courses.find(x => x.slug === slug);
    const file = course?.files.find(x => x.path === path);
    if (!course || !file) return;
    state.course = course; state.entry = { ...file, courseSlug: slug, courseTitle: course.titleFa };
    history.pushState({}, '', `#course=${encodeURIComponent(slug)}&file=${encodeURIComponent(path)}`); render();
  }));
  document.querySelectorAll('[data-section]').forEach(node => node.addEventListener('click', () => { state.section = node.dataset.section; render(); }));
  document.querySelector('#globalSearch')?.addEventListener('input', event => { state.query = event.target.value; render(false); });
}

function render(rebindInput = true) {
  if (!state.index) return;
  if (state.entry) { reader(state.entry); return; }
  content.innerHTML = state.course ? courseView(state.course) : dashboard();
  bindCards();
  if (!rebindInput) {
    const input = document.querySelector('#globalSearch');
    if (input) { input.focus(); input.setSelectionRange(input.value.length, input.value.length); }
  }
}

async function boot() {
  const theme = localStorage.getItem('as-academy-theme') || (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
  document.documentElement.dataset.theme = theme;
  try {
    const response = await fetch('data/index.json', { cache: 'no-cache' });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    state.index = await response.json();
    snapshotText.textContent = `MainCourse ${String(state.index.snapshotSha || '').slice(0,7)} • ${state.index.courses.length} دوره`;
    render();
  } catch (error) {
    snapshotText.textContent = 'خطا در بارگذاری';
    content.innerHTML = `<div class="error-card">نسخه وب نتوانست Index دوره‌ها را بخواند: ${esc(error.message)}</div>`;
  }
  if ('serviceWorker' in navigator) navigator.serviceWorker.register('sw.js').catch(() => {});
}

boot();
