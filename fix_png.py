import os
from PIL import Image

res_dir = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res'

for root, dirs, files in os.walk(res_dir):
    if 'mipmap' in root:
        for file in files:
            if file.endswith('.png'):
                filepath = os.path.join(root, file)
                try:
                    img = Image.open(filepath)
                    img = img.convert('RGBA')
                    img.save(filepath, 'PNG')
                except Exception as e:
                    print(f'Error processing {filepath}: {e}')
