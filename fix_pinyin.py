from pypinyin import pinyin, Style
import subprocess, json

mysql = r'"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql" -u root -proot --default-character-set=utf8mb4 r8479'

# Get all poems with content
result = subprocess.run(mysql + ' -e "SELECT id, content FROM course WHERE id>=21" --batch --raw',
                       shell=True, capture_output=True, text=True, encoding='utf-8')
lines = result.stdout.strip().split('\n')[1:]  # skip header

count = 0
for line in lines:
    parts = line.split('\t', 1)
    if len(parts) < 2: continue
    poem_id = parts[0]
    content = parts[1]

    # Generate pinyin with spaces between characters
    py_lines = []
    for text_line in content.split('\n'):
        text_line = text_line.strip()
        if not text_line: continue
        chars_py = []
        for ch in text_line:
            if '一' <= ch <= '鿿' or '㐀' <= ch <= '䶿':
                chars_py.append(pinyin(ch, style=Style.TONE)[0][0])
            else:
                chars_py.append(ch)
        py_lines.append(' '.join(chars_py))

    content_pinyin = '\n'.join(py_lines).replace("'", "\\'")

    sql = f"UPDATE course SET contentpinyin = '{content_pinyin}' WHERE id = {poem_id};"
    with open('tmp_fix.sql', 'w', encoding='utf-8') as f:
        f.write(sql)
    r = subprocess.run(mysql + ' < tmp_fix.sql', shell=True, capture_output=True, text=True)
    err = r.stderr.strip()
    if err and 'Warning' not in err:
        print(f"ERR id={poem_id}: {err[:80]}")
    count += 1

print(f"Fixed {count} poems")
