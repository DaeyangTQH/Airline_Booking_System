import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { airportService } from '../src/services/airportService.js';

const originalFetch = globalThis.fetch;
afterEach(() => { globalThis.fetch = originalFetch; });
const response = (data, status = 200) => ({
  ok: status >= 200 && status < 300, status,
  text: async () => JSON.stringify(data)
});

test('loads every page through Gateway and maps AirportView.name to the option label', async () => {
  const calls = [];
  const controller = new AbortController();
  globalThis.fetch = async (url, options) => {
    calls.push(url);
    assert.equal(options.signal, controller.signal);
    assert.equal(options.headers.Authorization, undefined);
    return response(calls.length === 1
      ? Array.from({ length: 100 }, (_, id) => ({ id, code: `A${id}`, name: 'Airport', city: 'City' }))
      : [{ id: 'han', code: 'HAN', name: 'Noi Bai', city: 'Hanoi', country: 'Vietnam' }]);
  };
  const airports = await airportService.list({ signal: controller.signal });
  assert.deepEqual(calls, ['/api/v1/airports?page=0&size=100', '/api/v1/airports?page=1&size=100']);
  assert.equal(airports.length, 101);
  assert.deepEqual(airports[100], {
    id: 'han', code: 'HAN', airport: 'Noi Bai', city: 'Hanoi', country: 'Vietnam'
  });
});

test('empty catalog returns an empty list without demo fallback', async () => {
  globalThis.fetch = async () => response([]);
  assert.deepEqual(await airportService.list(), []);
});

test('shared backend error is preserved', async () => {
  globalThis.fetch = async () => response({ code: 'UNAVAILABLE', message: 'Catalog unavailable' }, 503);
  await assert.rejects(airportService.list(), { message: 'Catalog unavailable', status: 503 });
});

test('malformed catalog is rejected', async () => {
  globalThis.fetch = async () => response({ content: [] });
  await assert.rejects(airportService.list(), /không hợp lệ/);
});
