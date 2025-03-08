This project was developed by a group of Computer Engineering students enrolled in the master's
course Software Platforms at the University of Padova. The primary objective of the project is
to build a platform for downloading articles from online newspapers, storing them in a database,
making them searchable through a search server, and extracting representations of topics discussed
in a set of articles returned as results for a given query.

Newspaper articles are fetched from the Guardian API, saved in MongoDB, made searchable via
Elasticsearch, and analyzed using Mallet for topic modeling. Additionally, the project aims at implementing a microservices architecture.

The application comprises a variety of services, that communicate with each-other.
