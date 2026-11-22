import { describe, it, before, after, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails
} = require('/usr/local/lib/node_modules/@firebase/rules-unit-testing');

const PROJECT_ID = 'demo-no-project';
let testEnv;

function validCheckpointPayload(userId, now = new Date(Date.now() - 1000)) {
  return {
    userId,
    displayName: 'Jucator Craciun',
    coins: 120,
    diamonds: 8,
    galactic: 2,
    currentLevel: 4,
    unlockedLevel: 4,
    completedLevels: [1, 2, 3],
    ownedHeroes: ['om_de_zapada', 'turta_dulce'],
    equippedHero: 'om_de_zapada',
    passXP: 240,
    premiumUnlocked: false,
    claimedFree: [1, 2],
    claimedPrem: [],
    lastDailyTs: 1700000000000,
    lastBonusQuestTs: 0,
    checkpointLevel: 4,
    checkpointX: 980.5,
    checkpointY: 210.0,
    statsLevelsDone: 3,
    statsIntrudersKilled: 4,
    statsCoinsGot: 120,
    statsJumpsMade: 45,
    bonusLevelsDone: 3,
    bonusIntrudersKilled: 4,
    bonusCoinsGot: 120,
    santaGiftsDelivered: 2,
    santaHousesVisited: 1,
    santaBubbleJumps: 12,
    santaSleighCoins: 20,
    claimedSantaQuests: ['sq_gifts'],
    grandSleighClaimed: false,
    createdAt: now,
    updatedAt: now
  };
}

describe('Christmas Parkour Firestore Security Rules', () => {
  before(async () => {
    const [host, portStr] = (process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8085').split(':');
    testEnv = await initializeTestEnvironment({
      projectId: PROJECT_ID,
      firestore: {
        host,
        port: Number(portStr),
        rules: fs.readFileSync('firestore.rules', 'utf8')
      }
    });
  });

  beforeEach(async () => {
    await testEnv.clearFirestore();
  });

  after(async () => {
    await testEnv.cleanup();
  });

  it('denies unauthenticated reads and writes', async () => {
    const guestDb = testEnv.unauthenticatedContext().firestore();
    const ref = guestDb.collection('users').doc('alice_123').collection('checkpoints').doc('main');
    await assertFails(ref.get());
    await assertFails(ref.set(validCheckpointPayload('alice_123')));
  });

  it('allows owner to create, read, partially update while preserving createdAt, and delete checkpoint', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_123').firestore();
    const ref = aliceDb.collection('users').doc('alice_123').collection('checkpoints').doc('main');

    const createdDate = new Date(Date.now() - 5000);
    await assertSucceeds(ref.set(validCheckpointPayload('alice_123', createdDate)));

    const snap = await assertSucceeds(ref.get());
    assert.equal(snap.exists, true);
    assert.equal(snap.data().currentLevel, 4);

    // Partial update must preserve createdAt and userId
    await assertSucceeds(
      ref.update({
        currentLevel: 5,
        unlockedLevel: 5,
        coins: 195,
        updatedAt: new Date(Date.now() - 1000)
      })
    );

    const updated = await assertSucceeds(ref.get());
    assert.equal(updated.data().currentLevel, 5);
    assert.equal(updated.data().coins, 195);

    await assertSucceeds(ref.delete());
  });

  it('denies cross-user read, create, update, and delete', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_123').firestore();
    const bobDb = testEnv.authenticatedContext('bob_456').firestore();
    const aliceRef = aliceDb.collection('users').doc('alice_123').collection('checkpoints').doc('main');
    const bobViewOfAlice = bobDb.collection('users').doc('alice_123').collection('checkpoints').doc('main');

    await assertSucceeds(aliceRef.set(validCheckpointPayload('alice_123')));

    await assertFails(bobViewOfAlice.get());
    await assertFails(bobViewOfAlice.update({ coins: 9999, updatedAt: new Date(Date.now() - 500) }));
    await assertFails(bobViewOfAlice.delete());

    // Spoofed userId inside own collection must also fail
    const bobOwnRef = bobDb.collection('users').doc('bob_456').collection('checkpoints').doc('main');
    await assertFails(bobOwnRef.set(validCheckpointPayload('alice_123')));
  });

  it('rejects ghost fields, immutable createdAt/userId tampering, invalid document IDs, and future timestamps', async () => {
    const aliceDb = testEnv.authenticatedContext('alice_123').firestore();
    const ref = aliceDb.collection('users').doc('alice_123').collection('checkpoints').doc('main');

    // Ghost field rejected on create
    const withGhost = { ...validCheckpointPayload('alice_123'), hackerMode: true };
    await assertFails(ref.set(withGhost));

    // Future timestamp rejected
    const futureDate = new Date(Date.now() + 60000);
    await assertFails(ref.set(validCheckpointPayload('alice_123', futureDate)));

    // Non-'main' checkpoint ID rejected
    const altRef = aliceDb.collection('users').doc('alice_123').collection('checkpoints').doc('backup');
    await assertFails(altRef.set(validCheckpointPayload('alice_123')));

    // Unspecified collection path rejected by global deny
    await assertFails(aliceDb.collection('other').doc('x').get());

    // Create valid doc, then try tampering with createdAt or userId
    await assertSucceeds(ref.set(validCheckpointPayload('alice_123', new Date(Date.now() - 4000))));
    await assertFails(ref.update({ createdAt: new Date(Date.now() - 100), updatedAt: new Date() }));
    await assertFails(ref.update({ userId: 'bob_456', updatedAt: new Date() }));
  });
});
