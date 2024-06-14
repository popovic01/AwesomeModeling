import datetime
import pika
import time
import os
import requests
import pymongo
import json
from bson import ObjectId

guardian_api = os.environ["GUARDIAN_API"]
print("Guardian api is {}".format(guardian_api))

base_url = "https://content.guardianapis.com/"

mongo_user = os.environ["MONGO_INITDB_ROOT_USERNAME"]
mongo_passwd = os.environ["MONGO_INITDB_ROOT_PASSWORD"]
client = pymongo.MongoClient("mongodb://{}:{}@mongo:27017/".format(mongo_user, mongo_passwd))
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


def guardian_search(query: str, id:str, fromdate: str|None = None, todate: str|None = None):
    first = guardian_search_page(query, 1, fromdate, todate)
    store_page(query, id, first)
    pages = first["response"]["pages"]
    for page in range(2, pages+1, 1):
        res = guardian_search_page(query, page, fromdate, todate)
        store_page(query, id, res)
        

def store_page(query: str , id: str, page):
    for result in page["response"]["results"]:
        store_article(query, id, result)

def store_article(query: str, id: str, article):
    title = article["webTitle"]
    content = article["blocks"]["body"][0]["bodyTextSummary"]
    guardian_id = article["id"] 
    web_date = article["webPublicationDate"]
    print(title)

    doc = {
        "title": title,
        "content" : content,
        "guardian_id" : guardian_id,
        "web_date": web_date
    }

    collection = db["articles_{}".format(id)]
    collection.insert_one(doc)
    print("Succesfully stored document")

    try:
        del doc["_id"]
        res = requests.post("http://elastic:9200/articles_{}/_doc".format(id), json.dumps(doc), headers={"Content-Type": "application/json"})
        res.raise_for_status()
        print("Succesfully indexed document")
    except Exception as err:
        print(err)


def callback_q1_message(ch, method, properties, body):
    print(f" [x] Received {body}")
    id = body.decode('utf-8')
    collection = db["control"]

    control_document = collection.find_one({"_id": ObjectId(id)})

    print("Retrieved control document: {}".format(control_document))

    query = control_document["topic"]

    collection.update_one({"_id": ObjectId(id)}, {"$set": {"status": "PROCESSING"}})

    guardian_search(query, id)

    collection.update_one({"_id": ObjectId(id)}, {"$set": {"status": "FINISHED", "finished_time": datetime.datetime.now(tz=datetime.timezone.utc)}})


def main():
    # print(guardian_search("science"))

    connection = pika.BlockingConnection(pika.ConnectionParameters(host='rabbit'))
    channel = connection.channel()

    channel.queue_declare(queue='q1')

    channel.basic_consume(queue='q1',
                        auto_ack=True,
                        on_message_callback=callback_q1_message)

    channel.start_consuming()

if __name__ == "__main__":
    main()
