ExpertFinding
=============

Requirements: input a it related topic and the system should return a list of experts from twitter.

=============

Implementation:

1.  Combined MySQL and Neo4j to store 1.44GB internal related datasets of experts by accessing Twitter RESTful API. Firstly, I used python to store those experts and the relationship between them according to some public accounts' followers. Then I transferred the timeline information to the MySQL database. 

2.	I used Lucene to create the index for each tweet rather than each timeline because in this way the experts will be returned more precisely. So when a user input a topic, the system will pull all relative tweets. Then each tweet will have a weight score and I added those scores back to experts so that each expert will get a weight score. This process is done by using language model and there are 6 million tweets. Results can be returned without delay by inputing normal topic query. Sometimes the process time maybe longer due the hot topics.
	
=============

Next Step (TODO):

Relationships between Experts should also be considered and I already set the foundation (Neo4j). I need to think carefully about how to calculate relationship's weight score.


