#sg
#http://api.worldbank.org/v2/en/indicator/ag.lnd.agri.zs?downloadformat=csv is the standard
#format of download urls, we will exploit this to download the data.

for i in `cat indicatorlist.txt`;do
	url="http://api.worldbank.org/v2/en/indicator/$i?downloadformat=csv"
	echo "Fetching $url"
	wget $url
done
