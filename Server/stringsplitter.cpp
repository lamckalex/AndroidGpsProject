#include <stdio.h>
#include <string.h>

struct pdata
{
  char* plong;
  char* plat;
  char* pip;
  char* ptime;
};

pdata rawToPData(char* str);

int main ()
{

  char str[] ="long,lat,ip,time";
  rawToPData(str);

  return 0;
}

/*
Alex's string to char* parser
*/
pdata rawToPData(char* str)
{
  pdata p;

  char * pch;
  printf ("Splitting string \"%s\" into tokens:\n",str);
  pch = strtok (str," ,.-");

  p.plong = pch;
  pch = strtok (NULL, " ,.-");
  p.plat = pch;
  pch = strtok (NULL, " ,.-");
  p.pip = pch;
  pch = strtok (NULL, " ,.-");
  p.ptime = pch;
  pch = strtok (NULL, " ,.-");

  printf("%s\n", p.plong);
  printf("%s\n", p.plat);
  printf("%s\n", p.pip);
  printf("%s\n", p.ptime);

}
