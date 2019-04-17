from mininet.topo import Topo
from mininet.net import Mininet
from mininet.util import dumpNodeConnections
from mininet.log import setLogLevel
from mininet.node import OVSController, RemoteController
from mininet.cli import CLI

# core switches
cores = []
# edge switched
switches = []
# host servers
hosts = []


class SingleSwitchTopo(Topo):
    def __init__(self, host=2, switch=4, core=4):
        Topo.__init__(self)

        for c in range(core):
            cores.append(self.addSwitch('core%s' % c))

        for s in range(switch):
            switches.append(self.addSwitch('switch%s' % s))
            for c in range(core):
                self.addLink(switches[s], cores[c])

        for h in range(host):
            hosts.append(self.addHost('host%s' % h))
            self.addLink(hosts[h], switches[h / switch])


def run_simulation():
    # "Create and test a simple network"
    topo = SingleSwitchTopo(host=16, switch=4, core=4)
    net = Mininet(topo=topo)
    net.start()
    print "Dumping host connections"
    dumpNodeConnections(net.hosts)
    print "Testing network connectivity"
    CLI(net)
    # net.pingAll()
    net.stop()


if __name__ == '__main__':
    # Tell mininet to print useful information
    setLogLevel('info')
    run_simulation()
