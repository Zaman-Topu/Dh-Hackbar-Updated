import os
import xml.etree.ElementTree as ET

layout_dir = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res\layout'
android_ns = 'http://schemas.android.com/apk/res/android'
ET.register_namespace('android', android_ns)

# Map element IDs to standard android drawables
icon_mapping = {
    'undo': '@android:drawable/ic_menu_revert',
    'redo': '@android:drawable/ic_menu_forward',
    'note': '@android:drawable/ic_menu_edit',
    'showhide': '@android:drawable/ic_menu_view',
    'menu': '@android:drawable/ic_menu_more',
    'findprev': '@android:drawable/ic_media_previous',
    'findnext': '@android:drawable/ic_media_next',
    'findclose': '@android:drawable/ic_menu_close_clear_cancel',
    'pagefavicon': '@android:drawable/ic_menu_info_details',
    'back': '@android:drawable/ic_menu_revert',
    'save': '@android:drawable/ic_menu_save',
    'add': '@android:drawable/ic_menu_add'
}

for filename in os.listdir(layout_dir):
    if filename.endswith('.xml'):
        filepath = os.path.join(layout_dir, filename)
        
        tree = ET.parse(filepath)
        root = tree.getroot()
        changed = False

        for elem in root.iter():
            id_attr = elem.get(f'{{{android_ns}}}id')
            if id_attr:
                clean_id = id_attr.replace('@+id/', '').replace('@id/', '')
                
                src_attr = elem.get(f'{{{android_ns}}}src')
                if src_attr and 'default_image' in src_attr:
                    if clean_id in icon_mapping:
                        elem.set(f'{{{android_ns}}}src', icon_mapping[clean_id])
                        changed = True
                    else:
                        elem.set(f'{{{android_ns}}}src', '@android:drawable/ic_menu_compass')
                        changed = True

        if changed:
            tree.write(filepath, encoding='utf-8', xml_declaration=True)
            print(f"Fixed icons in {filename}")
