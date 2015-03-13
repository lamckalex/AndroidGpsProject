/*-----------------------------------------------------------------------
--
--
--
--
--
-- REFERENCES:
-- https://akrzemi1.wordpress.com/2011/07/13/parsing-xml-with-boost/
-- http://www.boost.org/doc/libs/1_57_0/doc/html/property_tree.html	
--------------------------------------------------------------------------*/
#include "xmlmanip.h"

/*-----------------------------------------------------------------------
--  FUNCTION: readXML
--
--  NOTES: reads XML data in from input.txt into a tree and returns the
--  tree as a vector of Locations 
--
--
--------------------------------------------------------------------------*/
Locations readXML ( std::istream& istream )
{
	//create a tree structure
	using boost::property_tree::ptree;
	ptree location_data;
	read_xml(istream, location_data);

	//create a vector of locations for storing XML data
	Locations locationList;
	
	//traverse the tree structure
	BOOST_FOREACH ( ptree::value_type const& value, location_data.get_child("coordinates"))
	{
		//create a new location
		Location location;

		//add values to the location
		location.longitude = value.second.get<double>("long");
		location.latitude = value.second.get<double>("lat");
		location.time = value.second.get<std::string>("time");
		location.ip_address = value.second.get<std::string>("ip");
		location.mac_address = value.second.get<std::string>("mac");

		//add the location to the list of locations
		locationList.push_back(location);
	}

	return locationList;
}


/*-----------------------------------------------------------------------
--  FUNCTION: writeXML
--
--  NOTES: Writes XML data stored in a vector of Locations into an XML
--  output file.
--
--------------------------------------------------------------------------*/
void writeXML ( Locations locationList, std::ostream& ostream )
{
	///create a tree
	using boost::property_tree::ptree;
	ptree location_data;

	//given a locatonList, put data into the list
	BOOST_FOREACH ( Location location, locationList )
	{
		ptree& node = location_data.add("coordinates.coord", "");


		node.put("long", location.longitude);
		node.put("lat", location.latitude);
		node.put("ip", location.ip_address);
		node.put("time", location.time);
		node.put("mac", location.mac_address);
	}

	//then write the XML file into the stream
	write_xml( ostream, location_data);
}
