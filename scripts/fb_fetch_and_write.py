import os, json, sys
# Load .env
if os.path.exists('.env'):
    with open('.env') as f:
        for line in f:
            if '=' in line and not line.startswith('#'):
                k,v=line.strip().split('=',1)
                os.environ[k]=v
access_token=os.getenv('FACEBOOK_ACCESS_TOKEN')
page_id=os.getenv('FACEBOOK_PAGE_ID')
post_id=f"{page_id}_1584698486991971"
import urllib.request, ssl
ctx=ssl.create_default_context()
url=f'https://graph.facebook.com/v23.0/{post_id}?fields=id,message,created_time,permalink_url&access_token={access_token}'
try:
    data=json.loads(urllib.request.urlopen(url, context=ctx).read().decode())
except Exception as e:
    print('Error fetching post:',e)
    sys.exit(1)
# Prepare markdown
message=data.get('message','')
title_line=message.split('\n')[0][:70]
pubdate=data.get('created_time')
description=message[:200]
content=f"---\n" \
        f"title: \"{title_line}\"\n" \
        f"pubDate: {pubdate}\n" \
        f"description: \"{description}\"\n" \
        f"---\n\n{message}"
# Write file
path='src/posts/aggiornamento-rally-di-romagna.md'
with open(path,'w',encoding='utf-8') as f:
    f.write(content)
print('Created',path)
