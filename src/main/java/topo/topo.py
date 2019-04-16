from mininet.topo import Topo
from mininet.net import Mininet
from mininet.util import dumpNodeConnections
from mininet.log import setLogLevel
from mininet.node import CPULimitedHost
from mininet.link import TCLink
from mininet.node import OVSController


class SingleSwitchTopo(Topo):
    # "Single switch connected to n hosts."
    def build(self, host=2, switch=4, core=4):
        print host
        print switch
        print core

        cores = []
        for c in range(core):
            cores.append(self.addSwitch('core%s' % c))
        switches = []
        for s in range(switch):
            switches.append(self.addSwitch('switch%s' % s))
            for c in range(core):
                self.addLink(switches[s], cores[c])

        for h in range(host):
            host = self.addHost('host%s' % h)
            self.addLink(host, switches[h / switch])


def run_simulation():
    # "Create and test a simple network"
    topo = SingleSwitchTopo(host=16, switch=4, core=4)
    net = Mininet(topo=topo, host=CPULimitedHost, link=TCLink, controller=OVSController)
    net.start()
    print "Dumping host connections"
    dumpNodeConnections(net.hosts)
    print "Testing network connectivity"
    net.pingAll()
    net.stop()


if __name__ == '__main__':
    # Tell mininet to print useful information
    setLogLevel('info')
    run_simulation()
