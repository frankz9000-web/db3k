import os, sys
import json
import requests

def download_media(post_id):
    token = os.getenv('FACEBOOK_ACCESS_TOKEN')
    if not token:
        print('[!] FACEBOOK_ACCESS_TOKEN not set', file=sys.stderr)
        return
    url = f'https://graph.facebook.com/v23.0/{post_id}'
    params = {
        'access_token': token,
        'fields': 'attachments{media,subattachments}',
    }
    r = requests.get(url, params=params)
    if r.status_code!=200:
        print('[!] error', r.text,file=sys.stderr);return
    data=r.json()
    attachments=data.get('attachments',{}).get('data',[])
    if not attachments:
        print('[!] no attachments')
        return
    # ensure folder
    out_dir=os.path.join('public','fb',post_id)
    os.makedirs(out_dir,exist_ok=True)
    idx=1
    for att in attachments:
        media=att.get('media',{}).get('image',{}) or att.get('media',{}).get('source')
        if not media:continue
        url_media=media if isinstance(media,str) else media.get('src')
        if not url_media:continue
        fname=os.path.join(out_dir,f'{idx}{os.path.splitext(url_media)[1]}')
        with open(fname,'wb') as f:
            f.write(requests.get(url_media).content)
        print('[+] downloaded',fname)
        idx+=1
    print('done')
if __name__=='__main__':
    if len(sys.argv)<2:print('usage post_id');sys.exit(1)
    download_media(sys.argv[1])
