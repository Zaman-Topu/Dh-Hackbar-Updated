import os
import xml.etree.ElementTree as ET

filepath = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res\layout\hackbar.xml'
android_ns = 'http://schemas.android.com/apk/res/android'
app_ns = 'http://schemas.android.com/apk/res-auto'
ET.register_namespace('android', android_ns)
ET.register_namespace('app', app_ns)

tree = ET.parse(filepath)
root = tree.getroot()

# Update background color logic and layout styling
for ll in root.iter('LinearLayout'):
    if ll.get(f'{{{android_ns}}}id') == '@+id/background':
        ll.set(f'{{{android_ns}}}background', '?android:attr/colorBackground')

# Update TopBar
for ll in root.iter('LinearLayout'):
    if ll.get(f'{{{android_ns}}}id') == '@+id/topbar':
        # Change inner to Material styling
        ll.set(f'{{{android_ns}}}background', '?attr/colorSurface')
        ll.set(f'{{{android_ns}}}elevation', '4dp')

# Update buttons to modern Material 3 Tonal buttons
for btn in root.iter('com.google.android.material.button.MaterialButton'):
    btn.set('style', '?attr/materialButtonOutlinedStyle')
    btn.set(f'{{{app_ns}}}strokeColor', '?attr/colorOutline')
    btn.set(f'{{{android_ns}}}textColor', '?attr/colorPrimary')
    btn.set(f'{{{app_ns}}}cornerRadius', '12dp')
    btn.set(f'{{{android_ns}}}layout_margin', '4dp')
    btn.set(f'{{{android_ns}}}minHeight', '40dp')
    
# Clean up EditText backgrounds
for et in root.iter('EditText'):
    if et.get(f'{{{android_ns}}}background') == '@drawable/edittext_bg':
        # Don't use raw drawables, use M3 styling where possible
        pass

tree.write(filepath, encoding='utf-8', xml_declaration=True)
print('Applied basic Material 3 XML properties.')
