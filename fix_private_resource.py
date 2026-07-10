import os
import xml.etree.ElementTree as ET

layout_dir = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res\layout'
android_ns = 'http://schemas.android.com/apk/res/android'
ET.register_namespace('android', android_ns)

for filename in os.listdir(layout_dir):
    if filename.endswith('.xml'):
        filepath = os.path.join(layout_dir, filename)
        
        tree = ET.parse(filepath)
        root = tree.getroot()
        changed = False

        for elem in root.iter():
            src_attr = elem.get(f'{{{android_ns}}}src')
            if src_attr and 'ic_menu_forward' in src_attr:
                elem.set(f'{{{android_ns}}}src', '@android:drawable/ic_media_next')
                changed = True

        if changed:
            tree.write(filepath, encoding='utf-8', xml_declaration=True)
            print(f"Fixed {filename}")
