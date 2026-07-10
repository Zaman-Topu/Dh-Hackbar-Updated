import os
import xml.etree.ElementTree as ET

filepath = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res\layout\hackbar.xml'
android_ns = 'http://schemas.android.com/apk/res/android'
app_ns = 'http://schemas.android.com/apk/res-auto'
ET.register_namespace('android', android_ns)
ET.register_namespace('app', app_ns)

tree = ET.parse(filepath)
root = tree.getroot()

# Convert ScrollView LinearLayouts to ChipGroups and Buttons to Chips
for row_id in ['@+id/row1', '@+id/row2', '@+id/row3', '@+id/row4']:
    for scroll in root.findall('.//HorizontalScrollView'):
        for ll in scroll.findall('LinearLayout'):
            if ll.get(f'{{{android_ns}}}id') == row_id:
                # Change LinearLayout to ChipGroup
                ll.tag = 'com.google.android.material.chip.ChipGroup'
                ll.set(f'{{{app_ns}}}singleLine', 'true')
                ll.set(f'{{{android_ns}}}padding', '8dp')
                
                # Change MaterialButton to Chip
                for btn in ll.findall('com.google.android.material.button.MaterialButton'):
                    btn.tag = 'com.google.android.material.chip.Chip'
                    btn.set('style', '@style/Widget.Material3.Chip.Suggestion')
                    
                    # Remove button specific attributes
                    for attr in [f'{{{app_ns}}}strokeColor', f'{{{app_ns}}}cornerRadius', f'{{{android_ns}}}minHeight']:
                        if attr in btn.attrib:
                            del btn.attrib[attr]
                    
                    # Keep margins small for chips
                    btn.set(f'{{{android_ns}}}layout_margin', '2dp')

tree.write(filepath, encoding='utf-8', xml_declaration=True)
print('Converted tools to Material 3 Chips')
