import fs from 'node:fs';
import path from 'node:path';

const [coursesRoot, outputPath] = process.argv.slice(2);
if (!coursesRoot || !outputPath) {
  console.error('Usage: node build-index.mjs <courses-root> <output-json>');
  process.exit(1);
}

const TEXT_EXTENSIONS = new Set([
  '.json','.md','.txt','.kt','.kts','.java','.py','.js','.mjs','.cjs','.ts','.tsx','.jsx','.html','.css','.scss',
  '.sql','.sh','.yaml','.yml','.xml','.toml','.ini','.properties','.c','.h','.cpp','.hpp','.cs','.php','.dart','.go','.rs'
]);

function walk(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const full = path.join(dir, entry.name);
    if (entry.name.startsWith('.')) return [];
    return entry.isDirectory() ? walk(full) : [full];
  });
}

function collectStrings(value, out = []) {
  if (typeof value === 'string') out.push(value);
  else if (Array.isArray(value)) value.forEach(item => collectStrings(item, out));
  else if (value && typeof value === 'object') Object.values(value).forEach(item => collectStrings(item, out));
  return out;
}

function firstObject(value) {
  if (Array.isArray(value)) return value.find(item => item && typeof item === 'object') || null;
  return value && typeof value === 'object' ? value : null;
}

function sectionFor(relativePath) {
  const p = relativePath.replaceAll('\\','/').toLowerCase();
  const rules = [
    ['lessons','lessons'],['lesson','lessons'],['exercises','exercises'],['exercise','exercises'],['quizzes','quizzes'],['quiz','quizzes'],
    ['projects','projects'],['project','projects'],['glossary','glossary'],['labs','labs'],['lab','labs'],['levels','levels'],['chapters','chapters'],
    ['references','references'],['reference','references']
  ];
  for (const [needle, section] of rules) if (p.includes(`/${needle}/`) || p.endsWith(`/${needle}.json`)) return section;
  return p.startsWith('course/') ? 'course' : 'other';
}

function titleFromFile(filePath, rel, parsed, text) {
  const obj = firstObject(parsed);
  const title = obj?.titleFa || obj?.title || obj?.name || obj?.term || obj?.question || obj?.label;
  if (title) return String(title);
  if (rel.endsWith('manifest.json') && parsed?.titleFa) return parsed.titleFa;
  const heading = text.split(/\r?\n/).find(line => /^#{1,3}\s+/.test(line));
  if (heading) return heading.replace(/^#{1,3}\s+/, '').trim();
  return path.basename(filePath, path.extname(filePath)).replaceAll('-', ' ').replaceAll('_', ' ');
}

function summarize(parsed, text) {
  const obj = firstObject(parsed);
  const candidate = obj?.summary || obj?.description || obj?.objective || obj?.subtitle || obj?.prompt || obj?.content;
  if (typeof candidate === 'string') return candidate.replace(/\s+/g,' ').trim().slice(0,260);
  const flat = parsed ? collectStrings(parsed).join(' ') : text;
  return flat.replace(/\s+/g,' ').trim().slice(0,260);
}

const courseDirs = fs.readdirSync(coursesRoot, { withFileTypes: true }).filter(entry => entry.isDirectory()).sort((a,b) => a.name.localeCompare(b.name));
const courses = [];
for (const entry of courseDirs) {
  const slug = entry.name;
  const root = path.join(coursesRoot, slug);
  let manifest = {};
  const manifestPath = path.join(root, 'course', 'manifest.json');
  if (fs.existsSync(manifestPath)) {
    try { manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8')); } catch {}
  }
  const files = [];
  for (const filePath of walk(root)) {
    const ext = path.extname(filePath).toLowerCase();
    if (!TEXT_EXTENSIONS.has(ext)) continue;
    const stat = fs.statSync(filePath);
    if (stat.size > 2_500_000) continue;
    const rel = path.relative(root, filePath).split(path.sep).join('/');
    let text = '';
    try { text = fs.readFileSync(filePath, 'utf8'); } catch { continue; }
    let parsed = null;
    if (ext === '.json') { try { parsed = JSON.parse(text); } catch {} }
    const searchSource = parsed ? collectStrings(parsed).join(' ') : text;
    files.push({
      path: rel,
      section: sectionFor(rel),
      title: titleFromFile(filePath, rel, parsed, text),
      summary: summarize(parsed, text),
      searchText: `${rel} ${searchSource}`.replace(/\s+/g,' ').slice(0,12000)
    });
  }
  const counts = {};
  for (const file of files) counts[file.section] = (counts[file.section] || 0) + 1;
  courses.push({
    slug,
    courseId: manifest.courseId || slug,
    titleFa: manifest.titleFa || slug,
    titleEn: manifest.titleEn || slug,
    version: manifest.version || manifest.curriculumVersion || null,
    counts,
    files
  });
}

const output = {
  schemaVersion: 1,
  snapshotSha: process.env.MAINCOURSE_SHA || 'unknown',
  generatedAt: new Date().toISOString(),
  courses
};
fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, JSON.stringify(output), 'utf8');
console.log(`Indexed ${courses.length} courses and ${courses.reduce((n,c) => n + c.files.length, 0)} text files.`);
