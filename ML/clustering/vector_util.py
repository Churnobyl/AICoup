import math

def calDistPoint(pos1, pos2):
    return math.sqrt((pos1[0] - pos2[0])**2 + (pos1[1] - pos2[1])**2)


def getVectorValueByCenter(dots):
    ret = []
    for dot in dots:
        ret.append([dot[0] - 0.5, dot[1] - 0.5])

    return ret


def getVectorSize(vect):
    return math.sqrt(vect[0]**2 + vect[1]**2)


def getVectorSizes(vects):
    ret = []
    for vec in vects:
        ret.append(math.sqrt(vec[0]**2 + vec[1]**2))
    
    return ret


def getAnglesByVectors(vects):
    ret = []

    for vec in vects:
        ret.append(math.atan2(vec[1], vec[0]))

    return ret

def normalizeVector(vects):
    ret = []
    sizes = getVectorSize(vects)
    for vec, size in zip(vects, sizes):
        ret.append([vec[0] / size, vec[1] / size])
    
    return ret


def productVector(v1, v2):
    return v1[0]*v2[0] + v1[1]*v2[1]


# def getAnglesByVectors(vectors):
#     return [math.acos(p / s) if v[1] > 0 else 2 * math.pi - math.acos(p/s) for p, s, v in zip(products, vectorSizes, clusterVects)]

def anglesToDegrees(angles):
    ret = []
    for angle in angles:
        ret.append(angle * 180 / math.pi)

    return ret


# cos 유사도 함수
def calcCosSimilarlity(v1, v2):
    return productVector(v1, v2) / (getVectorSize([v1])[0]*getVectorSize([v2])[0])

