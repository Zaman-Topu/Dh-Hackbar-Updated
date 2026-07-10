import os
import re

layout_dir = r'd:\dh hackabr apk edit\HackBar-Modernized\app\src\main\res\layout'

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
        'activityname': 'Activity'
    }
    return mapping.get(id_str, id_str.capitalize())

for filename in os.listdir(layout_dir):
    if filename.endswith('.xml'):
        filepath = os.path.join(layout_dir, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Remove hardcoded black colors which hide text in dark mode
        content = re.sub(r'\s*android:textColor="#000000"', '', content)
        content = re.sub(r'\s*android:textColorHint="#000000"', '', content)
        
        # Replace "Fuck Modders" hint
        content = re.sub(r'android:id="@+id/urlfield"(.*?)android:hint="Fuck Modders"', r'android:id="@+id/urlfield"\1android:hint="Enter URL here..."', content)
        content = re.sub(r'android:id="@+id/paramsfield"(.*?)android:hint="Fuck Modders"', r'android:id="@+id/paramsfield"\1android:hint="Post Data / Parameters"', content)
        content = re.sub(r'android:hint="Fuck Modders"', 'android:hint="Search..."', content) # Generic fallback for findfield etc.
        
        # Find all occurrences of android:text="Fuck" and replace based on the preceding android:id
        def text_replacer(match):
            id_val = match.group(1)
            proper_text = format_id_to_text(id_val)
            # return the whole match replacing only the text="Fuck" part
            return match.group(0).replace('android:text="Fuck"', f'android:text="{proper_text}"')
            
        # Regex to find elements that have an id and also text="Fuck"
        # It looks for id="@+id/SOMETHING" and then anywhere in the same tag text="Fuck"
        content = re.sub(r'<[^>]*android:id="@+id/([a-zA-Z0-9_]+)"[^>]*android:text="Fuck"[^>]*>', text_replacer, content)
        # Handle cases where text="Fuck" comes before id
        def text_replacer_rev(match):
            id_val = match.group(1)
            proper_text = format_id_to_text(id_val)
            return match.group(0).replace('android:text="Fuck"', f'android:text="{proper_text}"')
            
        content = re.sub(r'<[^>]*android:text="Fuck"[^>]*android:id="@+id/([a-zA-Z0-9_]+)"[^>]*>', text_replacer_rev, content)

        # Also replace standalone "Fuck" texts that might not have been caught
        content = content.replace('android:text="Fuck"', 'android:text="Click"')
        
        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed {filename}")
