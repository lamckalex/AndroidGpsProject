/*----------------------------------------------------------------------------------------------
-- SOURCE FILE:		xmlmanip.cpp
--
-- PROGRAM:			AndroidGps
--
-- REVISIONS:		none
--
-- DESIGNTER:		Sebastian Pelka A00870247
--
-- PROGRAMMER:		Sebastian Pelka A00870247
--
-- FUNCTIONS:
--	  Locations readXML ( std::istream& istream )
--    void writeXML ( Locations locationList, std::ostream& ostream )
--
-- NOTES: This file contains functions that are used to read/write XML files
--        to store GPS data. 
--
-- REFERENCES:
-- https://akrzemi1.wordpress.com/2011/07/13/parsing-xml-with-boost/
-- http://www.boost.org/doc/libs/1_57_0/doc/html/property_tree.html	
----------------------------------------------------------------------------------------------*/
#include "xmlmanip.h"

/*----------------------------------------------------------------------------------------------
--  FUNCTION:		readXML
--
--  DATE:			March 10, 2015
-- 
--  REVISIONS:		none
--
--  DESIGNER:		Sebastian Pelka A00870247
--
--  PROGRAMMER:		Sebastian Pelka A00870247
--
--  INTERFACE:		Locations readXML ( std::istream& istream )
--
--	PARAMS:			istream		a reference to a standard input stream object
--
--  RETURNS:		a list of locations that were in the XML file
--
--  NOTES: reads XML data in from input.txt into a tree and returns the
--  tree as a vector of Locations 
--
---------------------------------------------------------------------------------------------*/
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

		//add the location to the list of locations
		locationList.push_back(location);
	}

	return locationList;
}


/*----------------------------------------------------------------------------------------------
--  FUNCTION:		writeXML
--
--  DATE:			March 10, 2015
-- 
--  REVISIONS:		none
--
--  DESIGNER:		Sebastian Pelka A00870247
--
--  PROGRAMMER:		Sebastian Pelka A00870247
--
--  INTERFACE:		Locations readXML ( std::istream& istream )
--
--	PARAMS:			ostream		 a reference to a standard output stream object
--					locationList a vector containing Locations to be written as XML
--
--  RETURNS:		void
--
--  NOTES: Writes XML data stored in a vector of Locations into an XML
--  output file.
--
------------------------------------------------------------------------------------------------*/
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
	}

	//then write the XML file into the stream
	write_xml( ostream, location_data);
}
