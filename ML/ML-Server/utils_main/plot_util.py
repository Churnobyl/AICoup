import matplotlib.pyplot as plt

card_font = {
    
}
cluster_font = {
    'color': 'red'
}

def plotCardsInfo(cardInfo):
    for idx, label in enumerate(cardInfo):
        plt.text(label['x']+0.01, label['y']+0.01, f"{idx}-{label['class_id']}")
        plt.plot(label['x'], label['y'], 'bs')


def plotClusters(clusters):
    for k in clusters:
        plt.text(
            clusters[k]["center"][0],
            clusters[k]["center"][1],
            k, fontdict=cluster_font
            )
        plt.plot(
            clusters[k]["center"][0],
            clusters[k]["center"][1],
            'rx'
            )


def plotInformations(cardInfo=None, clusters=None, block=False, show=True, transform=False, save=False):
    if cardInfo is not None:
        plotCardsInfo(cardInfo)
    if clusters is not None:
        plotClusters(clusters)

    plt.axhline(0.5)
    plt.axvline(0.5)
    plt.axis((0, 1, 0, 1))

    if not block:
        plt.ion()
    if show:
        plt.show()
    if transform:
        pass
    if save:
        plt.savefig('savefig_default.png')