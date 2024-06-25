import subprocess
import time
import requests

def setup_application():
    """Setup the application using Docker Compose."""
    try:
        print("Starting the application with Docker Compose...")
        subprocess.run(['docker-compose', 'up', '--build', '--detach'], check=True)
        print("Application setup complete.")
    except subprocess.CalledProcessError as e:
        print(f"An error occurred while setting up the application: {e}")
        raise

def destroy_application():
    """Destroy the application using Docker Compose."""
    try:
        subprocess.run(['docker-compose', 'down'], check=True)
    except subprocess.CalledProcessError as e:
        print(f"An error occurred while setting up the application: {e}")
        raise

def run_tests(base_url, q1, q2, k):
    print("Creating query")
    # Create the q1
    res = requests.post(base_url+'q1', json = {
        'topic' : q1,
        'local_start_date': '2024-01-01',
        'local_end_date': '2024-01-31'
    })

    if res.status_code != 201:
        print("Can't create query")
        return

    print("Query submitted: {}".format(res.json()))
    id = res.json()['id']

    # Wait for processing to complete
    while True:
        print("Querying status")
        res = requests.get(base_url+'q1/{}'.format(id))
        if res.status_code != 200:
            print("Can't get status")
            return

        status = res.json()['status']

        print("Status is {}".format(status))

        if status == 'FINISHED':
            break

        print("Sleeping 5 seconds")
        time.sleep(5)

    # Run topic modelin


    res = requests.get(base_url+'q1/{}/q2'.format(id), params={
        'query': q2,
        'k': '{}'.format(k)
        })

    if res.status_code != 200:
        print("Can't run modeling")
        return

    jsonres = res.json()
    topics = jsonres['topics']

    if len(topics) != k:
        print("Returned wrong number of topics")
        return

    print("Succesfully returned topics")
    print(topics)


if __name__ == "__main__":
    api_url = "http://localhost:8080/"
    setup_application()
    run_tests(api_url, 'science', 'moon', 20)
    destroy_application()
