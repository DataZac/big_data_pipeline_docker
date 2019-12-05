import happybase
import random
connection = happybase.Connection('localhost', 9090)
connection.tables()
table = connection.table('sensorData')

for i in range(100):
	m = {'device':['A','B'][random.randint(0,1)], 'timestamp':random.randint(0,99999999), 'lat':random.randint(0,99999999),
		 'lon': random.randint(0,99999999), 'temp':random.randint(0,99999999)}
	print('inserting', m)
	table.put(m['device']+'_'+str(m['timestamp']),
		 {'measuredData:lat': str(m['lat']),
		  'measuredData:lon': str(m['lon']), 
		  'measuredData:temp': str(m['temp'])})
	print('done',i)

for k, data in table.scan():
   print (k, data)
   
