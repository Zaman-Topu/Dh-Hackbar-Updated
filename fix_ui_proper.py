import os
import xml.etree.ElementTree as ET

layout_dir = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res\layout'
android_ns = 'http://schemas.android.com/apk/res/android'
ET.register_namespace('android', android_ns)
ET.register_namespace('app', 'http://schemas.android.com/apk/res-auto')

def format_id_to_text(id_str):
    mapping = {
        'columncount': 'Column Count',
        'unionstatements': 'Union Statements',
        'basicstatements': 'Basic Statements',
        'dios': 'DIOS',
        'localvariable': 'Local Variable',
        'errorbased': 'Error Based',
        'printsystem': 'Print System',
        'doublequery': 'Double Query',
        'xpathinjection': 'XPath Injection',
        'mssql': 'MSSQL',
        'postgresql': 'PostgreSQL',
        'lfi': 'LFI',
        'rce': 'RCE',
        'xss': 'XSS',
        'customquery': 'Custom Query',
        'replace': 'Replace',
        'wafbypass': 'WAF Bypass',
        'orderbybypass': 'Order By Bypass',
        'unionselectbypass': 'Union Select Bypass',
        'urlbalancer': 'URL Balancer',
        'polygon': 'Polygon',
        'writablepath': 'Writable Path',
        'authbypass': 'Auth Bypass',
        'userprivileges': 'User Privileges',
        'uploader': 'Uploader',
        'extractlinks': 'Extract Links',
        'find': 'Find',
        'viewsource': 'View Source',
        'postdata': 'POST Data',
        'tamperdata': 'Tamper Data',
        'javascript': 'JavaScript',
        'noredirect': 'No Redirect',
        'adminfinder': 'Admin Finder',
        'adminscanner': 'Admin Scanner',
        'webtools': 'Web Tools',
        'useragent': 'User Agent',
        'back': 'Back',
        'forward': 'Forward',
        'clear': 'Clear',
        'execute': 'Execute',
        'reload': 'Reload',
        'stop': 'Stop',
        'copy': 'Copy',
        'paste': 'Paste',
        'appname': 'DroidHack Pro',
        'findcount': '0/0',
        'pagetitle': 'Loading...',
        'activityname': 'DroidHack Pro'
    }
    return mapping.get(id_str, id_str.capitalize())

for filename in os.listdir(layout_dir):
    if filename.endswith('.xml') and filename not in ['activity_main.xml', 'activity_permission.xml']:
        filepath = os.path.join(layout_dir, filename)
        
        # Read as text first to fix raw "Click" -> "Fuck" just in case we need to parse them, 
        # actually ElementTree will parse it fine.
        tree = ET.parse(filepath)
        root = tree.getroot()
        changed = False

        for elem in root.iter():
            id_attr = elem.get(f'{{{android_ns}}}id')
            if id_attr:
                # e.g., @+id/columncount
                clean_id = id_attr.replace('@+id/', '').replace('@id/', '')
                
                text_attr = elem.get(f'{{{android_ns}}}text')
                if text_attr and (text_attr == 'Click' or text_attr == 'Fuck'):
                    new_text = format_id_to_text(clean_id)
                    elem.set(f'{{{android_ns}}}text', new_text)
                    changed = True
                    
            # Also fix hints
            hint_attr = elem.get(f'{{{android_ns}}}hint')
            if hint_attr and (hint_attr == 'Search...' or hint_attr == 'Fuck Modders'):
                elem.set(f'{{{android_ns}}}hint', 'Search...')
                changed = True
                
            if id_attr == '@+id/urlfield' and hint_attr:
                elem.set(f'{{{android_ns}}}hint', 'Enter URL here...')
                changed = True
            elif id_attr == '@+id/paramsfield' and hint_attr:
                elem.set(f'{{{android_ns}}}hint', 'Post Data / Parameters')
                changed = True

        if changed:
            # write back
            tree.write(filepath, encoding='utf-8', xml_declaration=True)
            print(f"Fixed {filename}")

