import pika
import time
import os
import requests
import pymongo

guardian_api = os.environ["GUARDIAN_API"]
print("Guardian api is {}".format(guardian_api))

base_url = "https://content.guardianapis.com/"

client = pymongo.MongoClient("mongodb://root:root@mongo:27017/")
db = client["test"]

def guardian_search_page(query: str, page: int = 1, fromdate: str|None = None, todate: str|None = None):
    url = base_url + "/search"
    params =  {
        "api-key": guardian_api,
        "q": query,
        "page": page,
        "page-size": 50,
        "show-blocks": "all",
        "type": "article"
    }
    if fromdate != None:
        params["from-date"] = fromdate
    if todate != None:
        params["to-date"] = todate

    res = requests.get(url, params)
    return res.json()


def guardian_search(query: str, fromdate: str|None = None, todate: str|None = None):
    first = guardian_search_page(query, 1, fromdate, todate)
    store_page(query, first)
    pages = first["response"]["pages"]
    for page in range(2, pages+1, 1):
        res = guardian_search_page(query, page, fromdate, todate)
        store_page(query, res)
        

def store_page(query: str , page):
    for result in page["response"]["results"]:
        store_article(query, result)

def store_article(query: str, article):
    title = article["webTitle"]
    content = article["blocks"]["body"][0]["bodyTextSummary"]
    guardian_id = article["id"] 
    print(title)

    article = { "title": title, "content" : content, "guardian_id" : guardian_id }
    collection = db["article"]

    collection.insert_one(article)


def main():
    print(guardian_search("science"))

    connection = pika.BlockingConnection(pika.ConnectionParameters(host='rabbit'))
    channel = connection.channel()

    channel.queue_declare(queue='hello')

    def callback(ch, method, properties, body):
        print(f" [x] Received {body}")

    channel.basic_consume(queue='hello',
                        auto_ack=True,
                        on_message_callback=callback)

    print(' [*] Waiting for messages. To exit press CTRL+C')
    channel.start_consuming()

if __name__ == "__main__":
    main()
