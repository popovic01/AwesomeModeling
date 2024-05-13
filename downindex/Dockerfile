FROM python
COPY main.py .
COPY requirements.txt .
RUN ["pip", "install", "-r", "requirements.txt"]
ENTRYPOINT ["python", "-u", "main.py"]
