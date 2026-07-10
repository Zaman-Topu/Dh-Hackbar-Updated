import os
import xml.etree.ElementTree as ET

filepath = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res\layout\hackbar.xml'
android_ns = 'http://schemas.android.com/apk/res/android'
app_ns = 'http://schemas.android.com/apk/res-auto'
ET.register_namespace('android', android_ns)
ET.register_namespace('app', app_ns)

tree = ET.parse(filepath)
root = tree.getroot()

# Convert action TextViews to MaterialButton
for row_id in ['@+id/row1', '@+id/row2', '@+id/row3', '@+id/row4']:
    for row in root.iter():
        if row.get(f'{{{android_ns}}}id') == row_id:
            for tv in row.findall('TextView'):
                tv.tag = 'com.google.android.material.button.MaterialButton'
                tv.set('style', '?attr/materialButtonOutlinedStyle')
                tv.set(f'{{{android_ns}}}textColor', '@color/colorAccent')
                tv.set(f'{{{app_ns}}}strokeColor', '@color/colorDivider')
                tv.set(f'{{{app_ns}}}cornerRadius', '8dp')
                tv.set(f'{{{android_ns}}}layout_margin', '4dp')
                tv.set(f'{{{android_ns}}}minHeight', '36dp')
                # Remove old paddings
                for p in ['paddingLeft', 'paddingRight', 'paddingTop', 'paddingBottom']:
                    if f'{{{android_ns}}}{p}' in tv.attrib:
                        del tv.attrib[f'{{{android_ns}}}{p}']

# Modernize EditText fields
for inputlayout in root.iter():
    if inputlayout.get(f'{{{android_ns}}}id') == '@+id/inputlayout':
        # Find urlfield and paramsfield
        for et in inputlayout.findall('EditText'):
            et.set(f'{{{android_ns}}}background', '@drawable/edittext_bg')
            et.set(f'{{{android_ns}}}padding', '12dp')
            et.set(f'{{{android_ns}}}layout_margin', '8dp')
            et.set(f'{{{android_ns}}}textColor', '@color/colorOnBackground')
            et.set(f'{{{android_ns}}}textColorHint', '@color/colorOnSurfaceVariant')

tree.write(filepath, encoding='utf-8', xml_declaration=True)
print('UI Redesign Applied to hackbar.xml')
