import os
import xml.etree.ElementTree as ET

filepath = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res\layout\hackbar.xml'
android_ns = 'http://schemas.android.com/apk/res/android'
ET.register_namespace('android', android_ns)

tree = ET.parse(filepath)
root = tree.getroot()

# Fix layout weight mapping (1:2 ratio as requested)
for ll in root.iter('LinearLayout'):
    if ll.get(f'{{{android_ns}}}id') == '@+id/background':
        # The top portion gets weight 1.2
        ll.set(f'{{{android_ns}}}layout_weight', '1.2')
    elif ll.get(f'{{{android_ns}}}id') == '@+id/webviewcontainer':
        # The webview portion gets weight 2.0 (so it takes up more space)
        ll.set(f'{{{android_ns}}}layout_weight', '2.0')

tree.write(filepath, encoding='utf-8', xml_declaration=True)
print('UI Proportions fixed')
