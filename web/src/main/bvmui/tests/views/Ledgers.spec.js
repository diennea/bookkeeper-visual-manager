import { mount, createGetResponse, flushPromises } from '../TestUtils'
import Ledgers from '@/views/Ledgers'
import Ledger from '@/components/Ledger'

describe('Ledgers', () => {
    test('Test Ledgers count', async () => {
        createGetResponse(new RegExp('api/ledger/all/*'), {
            ledgers: [],
            totalSize: 1024,
            totalLedgers: 100
        });
        createGetResponse(new RegExp('api/bookie/*'), []);
        createGetResponse(new RegExp('api/topology/*'), {});
        const ledgersPage = mount(Ledgers);
        await flushPromises();

        let counter = ledgersPage.find('.v-alert .v-alert__content');
        let counterText = counter.text();

        expect(counterText).toContain('Found: 100 ledgers');
        expect(counterText).toContain('total size: 1 KB');
    })
    test('Test Ledgers content', async () => {
        createGetResponse(new RegExp('api/ledger/all/*'), {
            ledgers: [
                {id: '1', clusterName: 'def', description: 'desc', age: 1, length: 1024, writeQuorumSize: 3, ensembleSize: 5, ackQuorumSize: 2}
            ],
            totalSize: 1024,
            totalLedgers: 100
        });
        createGetResponse(new RegExp('api/bookie/*'), []);
        createGetResponse(new RegExp('api/topology/*'), {});
        const ledgersPage = mount(Ledgers);
        await flushPromises();

        let ledger = ledgersPage.findComponent(Ledger);

        let ledgerName = ledger.findAll('.bvm-tile__row').at(0);
        let ledgerSize = ledger.findAll('.bvm-tile__row').at(1);
        let ledgerReplication = ledger.findAll('.bvm-tile__row').at(2);

        expect(ledgerName.text()).toBe('Ledger 1 (def)');
        expect(ledgerSize.text()).toBe('Size 1 KB');
        expect(ledgerReplication.text()).toBe('E=5, W=3, A=2');
    })
});
