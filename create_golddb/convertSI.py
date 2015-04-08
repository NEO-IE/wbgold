#converts the given knowledge base to SI units
conversion = {}

conversion['AG.LND.TOTL.K2'] = 1000000
conversion['BN.KLT.DINV.CD'] = 1
conversion['BX.GSR.MRCH.CD'] = 1
conversion['EG.ELC.PROD.KH'] = 3600000
conversion['EN.ATM.CO2E.KT'] = 1000000
conversion['EP.PMP.DESL.CD'] = 1
conversion['FP.CPI.TOTL.ZG'] = 1
conversion['IT.NET.USER.P2'] = 1
conversion['NY.GDP.MKTP.CD'] = 1
conversion['SP.DYN.LE00.IN'] = 31557600
conversion['SP.POP.TOTL']    = 1

import sys
inputfile = sys.argv[1]
outputfile = sys.argv[2]

ofile = open(outputfile, "w")

with open(inputfile) as ifile:
    for line in ifile:
        parts = line.split()
        new_value = float(parts[1]) * float(conversion[parts[2]])
        ofile.write(parts[0] + "\t" + str(new_value) + "\t" + parts[2] + "\n")


ofile.close()
