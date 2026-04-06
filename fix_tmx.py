import re

with open('src/main/resources/assets/levels/Main_level.tmx', 'r', encoding='utf-8') as f:
    content = f.read()

# Tìm từng objectgroup và thêm type vào object không có type
def fix_objectgroup(content, group_name, type_name):
    # Pattern tìm objectgroup theo tên
    pattern = rf'(<objectgroup[^>]*name="{group_name}"[^>]*>)(.*?)(</objectgroup>)'
    
    def replace_group(m):
        inner = m.group(2)
        # Thêm type vào object chưa có type (không có type= sau object id=...)
        inner = re.sub(
            r'(<object id="\d+"(?! type=))',
            rf'\1 type="{type_name}"',
            inner
        )
        return m.group(1) + inner + m.group(3)
    
    return re.sub(pattern, replace_group, content, flags=re.DOTALL)

original = content
content = fix_objectgroup(content, 'Collisions', 'Collisions')
content = fix_objectgroup(content, 'Interaction', 'Interaction')

fixed = content.count('type="Collisions"') + content.count('type="Interaction"')
original_count = original.count('type="Collisions"') + original.count('type="Interaction"')
print(f'Added {fixed - original_count} type attributes')

with open('src/main/resources/assets/levels/Main_level.tmx', 'w', encoding='utf-8') as f:
    f.write(content)

print('Done')
