#ifndef XMLMANIP_H
#define XMLMANIP_H

#include <string>
#include <vector>
#include <iostream>
#include <fstream>
#include <iostream>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <boost/foreach.hpp>

using namespace std;

struct Location
{
	std::string time;
	std::string ip_address;
	double latitude;
	double longitude;
	std::string mac_address;
	std::string name;
};

typedef std::vector<struct Location> Locations;

//prototypes
Locations readXML ( std::istream& istream );
void writeXML ( Locations locationList, std::ostream& ostream );

#endif
