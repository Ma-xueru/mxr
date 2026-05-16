from pypinyin import pinyin, Style
import subprocess, re

mysql = r'"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql" -u root -proot --default-character-set=utf8mb4 r8479'

result = subprocess.run(mysql + ' -e "SELECT id, content FROM course WHERE id>=5" --batch --raw', shell=True, capture_output=True, text=True, encoding='utf-8')
rows = result.stdout.strip().split('\n')[1:]

count = 0
for row in rows:
    parts = row.split('\t', 1)
    if len(parts) < 2: continue
    pid, content = parts[0], parts[1]

    py_lines = []
    for line in content.split('\n'):
        line = line.strip()
        if not line: continue
        chars_py = []
        for ch in line:
            if '一' <= ch <= '鿿':  # CJK Unified
                chars_py.append(pinyin(ch, style=Style.TONE)[0][0])
            # Skip punctuation, just keep the character positions
        py_lines.append(' '.join(chars_py))

    py_text = '\n'.join(py_lines).replace("'", "\\'")

    sql = f"UPDATE course SET contentpinyin = '{py_text}' WHERE id = {pid};"
    with open('tmp_fix2.sql', 'w', encoding='utf-8') as f:
        f.write(sql)
    r = subprocess.run(mysql + ' < tmp_fix2.sql', shell=True, capture_output=True, text=True)
    count += 1

print(f"Fixed {count} poems")
