#reads the csv downloaded from the world bank and generates a kb file

def makeFreebaseMap(mapFile):
    from collections import defaultdict
    fbMap = defaultdict()
    for line in open(mapFile, "r"):
        lineSplit = line.strip("\n").split("\t")
        fbMap[lineSplit[1]] = lineSplit[0]
    return fbMap

def genKb(fileName, freeBaseMap):
    name_i=0
    indi_i=3
    SEP = "\t"
    for line in open(fileName, "r"):
        lineSplit = [l.strip("\"") for l in line.strip("\n").split(SEP)]
        vals = lineSplit[4:]
        for val in vals:
            if val:
                try:
                    print "%s\t%s\t%s" % (freeBaseMap[lineSplit[name_i]], val, lineSplit[indi_i])                  
                except KeyError:
                    None

if __name__ == '__main__':
    import sys
    csvFile = sys.argv[1]
    freeBaseMap = sys.argv[2]
    genKb(sys.argv[1], makeFreebaseMap(freeBaseMap))

