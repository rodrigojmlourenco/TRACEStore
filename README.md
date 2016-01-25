# TRACEStore
TRACEstore  is  a  module  of  TRACE  responsible  for  storing,  aggregating  and  mining  TRACE’s 
related  data  in  a  persistent  manner. TRACEstore  focuses  on  storing  and  aggregating  data  about 
road networks, cycling paths and walkways, and about TRACE’s users, namely their tracked data 
about multimodal trips and trajectories. Besides this data, TRACEstore also keeps track of personal 
details of both TRACE users and interested third parties, which  wish to contribute to the TRACE 
project by rewarding its users. These interested third parties are essentially entities that are part of a 
behavioural change campaign and thus require access to some of the data stored in TRACEstore. 

TRACEstore provides an API that allows external entities to write and read data from its storage. 
Since TRACEstore is responsible for data mining, interested third parties can leverage this API to 
execute complex query  lookups for semantic data. However, due to security concerns, both read 
and  write  operations  are  done  in  a  controlled  manner.
