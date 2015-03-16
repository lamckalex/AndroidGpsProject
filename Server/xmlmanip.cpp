/*****************************************************************************************************
**	SOURCE FILE:	xmlmanip.cpp	source file that contains functions for reading from and writing
**									to XML files.
**	 	
**	PROGRAM:	server
**
**	FUNCTIONS:
**		Locations readXML ( std::istream& istream )
**		void writeXML ( Locations locationList, std::ostream& ostream )
**
**	DATE: 		March 12, 2015
**
**	DESIGNERS: 	Sebastian Pelka 
**
**	PROGRAMMER: Sebastian Pelka
**
**  REFERENCES:
**  https://akrzemi1.wordpress.com/2011/07/13/parsing-xml-with-boost/
**  http://www.boost.org/doc/libs/1_57_0/doc/html/property_tree.html	
*********************************************************************************************************/
#include "xmlmanip.h"

/***************************************************************************
** Function:		readXML ( std::istream& istream )
**
** DATE:			March 12, 2015
**
** DESIGNER:		Sebastian Pelka
**
** PROGRAMMER:		Sebastian Pelka
**
** INTERFACE:		Locations readXML ( std::istream& istream )
**
** PARAMETERS:
**			istream		- input stream object that will read an XML file
**
** REVISIONS:
**			March 13, 2015 		-Modified by Filip to handle a fifth
**								 element: mad address
**
** RETURNS:	a vector of Location structs; the data from the XML file
**
** NOTES:
** Reads in data from an XML file into Location structs, which are then stored
** as a vector of Locations.
*****************************************************************************/
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

/***************************************************************************
** Function:		writeXML ( Locations locationList, std::ostream& ostream )
**
** DATE:			March 12, 2015
**
** DESIGNER:		Sebastian Pelka
**
** PROGRAMMER:		Sebastian Pelka
**
** INTERFACE:		void writeXML ( Locations locationList, std::ostream& ostream )
**
** PARAMETERS:
**			locationList- a vector of Location structs
**			ostream		- output stream object that will read an XML file
**
** REVISIONS:
**			March 13, 2015 		-Modified by Filip to handle a fifth
**								 element: mad address
**
** RETURNS:	void
**
** NOTES:
** Writes the contents of a Locations vector into an XML file.
*****************************************************************************/
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
