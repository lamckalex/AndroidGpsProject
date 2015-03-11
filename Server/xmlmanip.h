#ifndef XMLMANIP_H
#define XMLMANIP_H

#include <string>
#include <vector>
#include <iostream>
#include <fstream>

#include <iostream>

using namespace std;

struct Location
{
	std::string time;
	std::string ip_address;
	double latitude;
	double longitude;
};

typedef std::vector<struct Location> Locations;

//prototypes
Locations readXML ( std::istream& istream );
void writeXML ( Locations locationList, std::ostream& ostream );

#endif
