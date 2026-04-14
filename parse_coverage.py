import xml.etree.ElementTree as ET

tree = ET.parse('target/site/jacoco/jacoco.xml')
root = tree.getroot()

methods_to_check = {
    'addBcc': None,
    'addCc': None,
    'addHeader': None,
    'addReplyTo': None,
    'buildMimeMessage': None,
    'getHostName': None,
    'getMailSession': None,
    'getSentDate': None,
    'getSocketConnectionTimeout': None,
    'setFrom': None
}

for package in root.findall('package'):
    if package.attrib['name'] == 'org/apache/commons/mail':
        for clazz in package.findall('class'):
            if clazz.attrib['name'] == 'org/apache/commons/mail/Email':
                for method in clazz.findall('method'):
                    m_name = method.attrib['name']
                    if m_name in methods_to_check:
                        covered = 0
                        missed = 0
                        for counter in method.findall('counter'):
                            if counter.attrib['type'] == 'INSTRUCTION':
                                covered = int(counter.attrib['covered'])
                                missed = int(counter.attrib['missed'])
                        
                        total = covered + missed
                        if total > 0:
                            percent = (covered / total) * 100
                            print(f"{m_name} {method.attrib['desc']}: {percent:.2f}% (covered: {covered}, missed: {missed})")

