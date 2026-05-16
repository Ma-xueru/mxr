with open('apoetryreccsquizintelassist_wx/pages/course/detail.js', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix 1: replace author parsing with simple field read
old1 = """    // Get author from intro: "王维所作..." -> pick out name
    let author = '', dynasty = ''
    const intro = data.intro || ''
    const authorMatch = intro.match(/([（(]([^）)]+)[）)])?\s*(\S+)所/)
    if (authorMatch) {
      const raw = authorMatch[0].replace(/所作.*/, '')
      const dm = raw.match(/[（(]([^）)]+)[）)]/)
      if (dm) dynasty = dm[1]
      const am = raw.replace(/[（(][^）)]+[）)]/, '').replace(/所作/, '').trim()
      if (am) author = am
    }"""

new1 = """    const author = data.authorName || data.author_name || ''
    const dynasty = data.grade || ''"""

content = content.replace(old1, new1)

# Fix 2: replace annotations/translation parsing
old2 = """    // Parse annotations from content
    const annotations = []
    const fullText = data.intro || ''
    // Simple: use the content above the translation as annotations
    // For now extract key phrases
    if (fullText) {
      const parts = fullText.split(/注释[：:]|注解[：:]/)
      if (parts.length > 1) {
        const anotext = parts[1].split(/译文[：:]|翻译[：:]/)[0]
        anotext.split(/[；;]/).filter(s => s.trim()).forEach(s => annotations.push(s.trim()))
      }
    }

    // Parse translation
    let translation = ''
    if (fullText) {
      const tparts = fullText.split(/译文[：:]|翻译[：:]/)
      if (tparts.length > 1) translation = tparts[1].trim()
    }"""

new2 = """    // Use separate annotations/translation fields
    const annText = data.annotations || ''
    const annotations = annText.split(/[；;\\n]/).filter(s => s.trim())

    const translation = data.translation || ''"""

content = content.replace(old2, new2)

with open('apoetryreccsquizintelassist_wx/pages/course/detail.js', 'w', encoding='utf-8') as f:
    f.write(content)
print('OK')
