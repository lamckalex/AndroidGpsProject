/*------------------------------------------------------------------------------------------------------------------
-- SOURCE FILE: gmaps.js - A website that takes the coordinates.xml file and displays the results with googleamaps and a table.
--
-- PROGRAM: Android GPS Website
--
-- FUNCTIONS:
--  function initalize()
--  function loadXMLDoc(filename)
--  function makeTableRow(row, ip, time, longt, lat)
--  function makeTableHead()
--  function refresh()
--  function removeMarkers()
--  function addMarkers()
--  function mostRecentMarkers()
--  function ipHistoryMarkers(givenIP)
--  function allCurrent()
--  function singleIPHistory()
--  function refreshOn()
--  function refreshOff()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--
-- NOTES:
-- The site needs to be hosted on apache for it to work, just loading it via double clicking it does not work since
-- xml parsing needs it tob e hosted.
----------------------------------------------------------------------------------------------------------------------*/

var markersArray = [];
var map;
var refreshInterval;
var mode;

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function initialize()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--
-- INTERFACE: initialize()
--
-- RETURNS: void
--
-- NOTES:
-- Creates the map and centers it to bcit
----------------------------------------------------------------------------------------------------------------------*/
function initialize() {
  var bcitLatlng = new google.maps.LatLng(49.2504322,-122.9938279);
  var mapOptions = {
    zoom: 10,
    center: bcitLatlng
  }

  map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

  mode = "allCurrent";
  makeTableHead();
  mostRecentMarkers();
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function loadXMLDoc(filename)
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--
-- INTERFACE: function loadXMLDoc(filename)
--
-- RETURNS: void
--
-- NOTES:
-- Loads the xml file
----------------------------------------------------------------------------------------------------------------------*/
function loadXMLDoc(filename)
{
  if (window.XMLHttpRequest)
    {
    xhttp=new XMLHttpRequest();
    }
  else
    {
    xhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
  xhttp.open("POST",filename,false);
  xhttp.send();
  return xhttp.responseXML;
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function makeTableRow(row, ip, time, longt, lat)
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--             Sanders Lee
--
-- INTERFACE: function makeTableRow(row, mac, ip, time, longt, lat)
--
-- RETURNS: void
--
-- NOTES:
-- Creates a row in the table
----------------------------------------------------------------------------------------------------------------------*/
function makeTableRow(row, mac, ip, time, longt, lat)
{
    var table = document.getElementById("record");
    var row = table.insertRow(row+1);
    var cell1 = row.insertCell(0);
    var cell2 = row.insertCell(1);
    var cell3 = row.insertCell(2);
    var cell4 = row.insertCell(3);
    var cell5 = row.insertCell(4);
    
    cell1.innerHTML = mac;
    cell2.innerHTML = ip;
    cell3.innerHTML = time;
    cell4.innerHTML = Number(longt).toFixed(4);
    cell5.innerHTML = Number(lat).toFixed(4);
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function makeTableHead()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--             Sanders Lee
--
-- INTERFACE: function makeTableHead()
--
-- RETURNS: void
--
-- NOTES:
-- Creates the table header on the site
----------------------------------------------------------------------------------------------------------------------*/
function makeTableHead()
{
  var table = document.getElementById("record");
  var header = table.createTHead();
  var row = header.insertRow(0);
  var cell1 = row.insertCell(0);
  var cell2 = row.insertCell(1);
  var cell3 = row.insertCell(2);
  var cell4 = row.insertCell(3);
  var cell5 = row.insertCell(4);
  
  cell1.innerHTML = "<b>MAC</b>";
  cell2.innerHTML = "<b>IP</b>";
  cell3.innerHTML = "<b>TIME</b>";
  cell4.innerHTML = "<b>LONG</b>";
  cell5.innerHTML = "<b>LAT</b>";
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function refresh()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--
-- INTERFACE: function refresh()
--
-- RETURNS: void
--
-- NOTES:
-- Removes all markers and re-populates them
----------------------------------------------------------------------------------------------------------------------*/
function refresh()
{
  removeMarkers();

  if (mode == "allCurrent")
    mostRecentMarkers();
  else
    macHistoryMarkers(mode);
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function removeMarkers()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--
-- INTERFACE: function removeMarkers()
--
-- RETURNS: void
--
-- NOTES:
-- Remove all markers
----------------------------------------------------------------------------------------------------------------------*/
function removeMarkers()
{
  var myTable = document.getElementById("record");
  var rowCount = myTable.rows.length;
  for (var x=rowCount-1; x>0; x--) {
     myTable.deleteRow(x);
  }

  for (var i = 0; i < markersArray.length; i++ ) {
    markersArray[i].setMap(null);
  }
  markersArray.length = 0;
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function addMarkers()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--
-- INTERFACE: function addMarkers()
--
-- RETURNS: void
--
-- NOTES:
-- Add markers from the coordinates file
-- No longer used, replaced with
--    function mostRecentMarkers()
--    function ipHistoryMarkers(givenIP)
----------------------------------------------------------------------------------------------------------------------*/
function addMarkers()
{
  var xmlDoc = loadXMLDoc("coordinates.xml");
  var x=xmlDoc.getElementsByTagName("coord");

  for (var i=0;i<x.length;i++)
    {
      var ip = xmlDoc.getElementsByTagName("ip")[i].childNodes[0].nodeValue;
      var time = xmlDoc.getElementsByTagName("time")[i].childNodes[0].nodeValue;
      var longt = xmlDoc.getElementsByTagName("long")[i].childNodes[0].nodeValue;
      var lat = xmlDoc.getElementsByTagName("lat")[i].childNodes[0].nodeValue;

      var newCoord = new google.maps.LatLng(lat,longt);

      var marker = new google.maps.Marker({
          position: newCoord,
          map: map,
          title: name
      });

      markersArray.push(marker);

      makeTableRow(i, ip, time, longt, lat);
    }
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function mostRecentMarkers()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Sanders Lee
--
-- PROGRAMMER: Sanders Lee
--
-- INTERFACE: function mostRecentMarkers()
--
-- RETURNS: void
--
-- NOTES:
-- Gets most recent markers per MAC address
-- based on Alex's function addMarkers()
----------------------------------------------------------------------------------------------------------------------*/
function mostRecentMarkers()
{
    var xmlDoc = loadXMLDoc("coordinates.xml");
    var x = xmlDoc.getElementsByTagName("coord");

    var uniqueMACs = [];
    var uniqueCount = 0;

    // this for loop produces a list of unique MAC addresses
    for (var i = x.length-1; i >= 0; i--)
    {
        var mac = xmlDoc.getElementsByTagName("mac")[i].childNodes[0].nodeValue;
        var matchFound = false;
        if (i != x.length-1)
        {
            var matchFound = false;
            for (var j = 0; j < uniqueCount; j++)
            {
                if (mac == uniqueMACs[j])
                {
                    matchFound = true;
                    break;
                }
            }
        }
        if (!matchFound)
        {
            uniqueMACs[uniqueCount] = mac;
            uniqueCount++;
        }
    }

    // get latest information on each unique MAC address
    for (var j = 0; j < uniqueCount; j++)
    {
        for (var i = x.length-1; i >= 0; i--)
        {
            var mac = xmlDoc.getElementsByTagName("mac")[i].childNodes[0].nodeValue;
            if (mac == uniqueMACs[j])
            {
                var ip = xmlDoc.getElementsByTagName("ip")[i].childNodes[0].nodeValue;
                var time = xmlDoc.getElementsByTagName("time")[i].childNodes[0].nodeValue;
                var longt = xmlDoc.getElementsByTagName("long")[i].childNodes[0].nodeValue;
                var lat = xmlDoc.getElementsByTagName("lat")[i].childNodes[0].nodeValue;

                var newCoord = new google.maps.LatLng(lat, longt);

                var marker = new google.maps.Marker({
                    position: newCoord,
                    map: map,
                    title: name
                });

                markersArray.push(marker);

                makeTableRow(j, mac, ip, time, longt, lat);
                break;
            }
        }
    }
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function macHistoryMarkers(givenMAC)
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Sanders Lee
--
-- PROGRAMMER: Sanders Lee
--
-- INTERFACE: function macHistoryMarkers(givenMAC)
--
-- RETURNS: void
--
-- NOTES:
-- Gets all markers based on the given MAC address
-- based on Alex's function addMarkers()
----------------------------------------------------------------------------------------------------------------------*/
function macHistoryMarkers(givenMAC)
{
  var xmlDoc = loadXMLDoc("coordinates.xml");
  var x = xmlDoc.getElementsByTagName("coord");
  var rowCounter = 0;

  for (var i = x.length-1; i >= 0; i--)
  {
    var mac = xmlDoc.getElementsByTagName("mac")[i].childNodes[0].nodeValue;
    if(givenMAC != mac)
        continue;
        
    var ip = xmlDoc.getElementsByTagName("ip")[i].childNodes[0].nodeValue;
    var time = xmlDoc.getElementsByTagName("time")[i].childNodes[0].nodeValue;
    var longt = xmlDoc.getElementsByTagName("long")[i].childNodes[0].nodeValue;
    var lat = xmlDoc.getElementsByTagName("lat")[i].childNodes[0].nodeValue;

    var newCoord = new google.maps.LatLng(lat,longt);

    var marker = new google.maps.Marker({
        position: newCoord,
        map: map,
        title: name
    });

    // mark the latest point red, but previous points green
    if (rowCounter == 0)
        marker.setIcon('http://maps.google.com/mapfiles/ms/icons/red-dot.png');
    else
        marker.setIcon('http://maps.google.com/mapfiles/ms/icons/green-dot.png');

    markersArray.push(marker);
    makeTableRow(rowCounter, mac, ip, time, longt, lat);
    rowCounter++;
  }
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function allCurrent()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Sanders Lee
--
-- PROGRAMMER: Sanders Lee
--
-- INTERFACE: function allCurrent()
--
-- RETURNS: void
--
-- NOTES:
-- Changes the mode of the website to all current mode.
----------------------------------------------------------------------------------------------------------------------*/
function allCurrent()
{
    mode = "allCurrent";
    refresh();
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function singleMACHistory()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Sanders Lee
--
-- PROGRAMMER: Sanders Lee
--
-- INTERFACE: function singleMACHistory()
--
-- RETURNS: void
--
-- NOTES:
-- Changes the mode of the website to single MAC mode.
----------------------------------------------------------------------------------------------------------------------*/
function singleMACHistory()
{
    mode = " "+document.getElementById("macAddress").value;
    refresh();
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function refreshOn()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--
-- INTERFACE: function refreshOn()
--
-- RETURNS: void
--
-- NOTES:
-- Sets the website to refresh the markers
----------------------------------------------------------------------------------------------------------------------*/
function refreshOn()
{
  refreshInterval = setInterval(function () {refresh()}, 1000);
}

/*------------------------------------------------------------------------------------------------------------------
-- FUNCTION: function refreshOff()
--
-- DATE: March 13, 2015
--
-- REVISIONS: (Date and Description)
--
-- DESIGNER: Alex Lam
--
-- PROGRAMMER: Alex Lam
--
-- INTERFACE: function refreshOff()
--
-- RETURNS: void
--
-- NOTES:
-- Disables refresh interval
----------------------------------------------------------------------------------------------------------------------*/
function refreshOff()
{
  clearInterval(refreshInterval);
}

google.maps.event.addDomListener(window, 'load', initialize);
