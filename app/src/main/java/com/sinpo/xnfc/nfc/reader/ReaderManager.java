/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.sinpo.xnfc.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.util.Log;

import com.sinpo.xnfc.SPEC;
import com.sinpo.xnfc.nfc.Util;
import com.sinpo.xnfc.nfc.bean.Card;
import com.sinpo.xnfc.nfc.reader.pboc.StandardPboc;

public final class ReaderManager extends AsyncTask<Tag, SPEC.EVENT, Card> {

	public static void readCard(Tag tag, ReaderListener listener) {
		new ReaderManager(listener).execute(tag);
	}

	private ReaderListener realListener;

	private ReaderManager(ReaderListener listener) {
		realListener = listener;
	}

	@Override
	protected Card doInBackground(Tag... detectedTag) {
		return readCard(detectedTag[0]);
	}

	@Override
	protected void onProgressUpdate(SPEC.EVENT... events) {
		if (realListener != null)
			realListener.onReadEvent(events[0]);
	}

	@Override
	protected void onPostExecute(Card card) {
		if (realListener != null)
			realListener.onReadEvent(SPEC.EVENT.FINISHED, card);
	}

	private static final String TAG = "nfc";
	private Card readCard(Tag tag) {

		final Card card = new Card();

		try {

			publishProgress(SPEC.EVENT.READING);
			Log.e(TAG, "读卡的hexString应用: "+Util.toHexString(tag.getId()) );

			card.setProperty(SPEC.PROP.ID, Util.toHexString(tag.getId()));

			final IsoDep isodep = IsoDep.get(tag);
			if (isodep != null)
            {Log.e(TAG, "IsoDep人民银行 标准: ");
				//人民银行 标准
				StandardPboc.readCard(isodep, card);}

			final NfcF nfcf = NfcF.get(tag);
			if (nfcf != null)
            {Log.e(TAG, "NfcF非接触IC ，电子钱包 标准: ");
			//非接触IC ，电子钱包
				FelicaReader.readCard(nfcf, card);}

			publishProgress(SPEC.EVENT.IDLE);

		} catch (Exception e) {
			card.setProperty(SPEC.PROP.EXCEPTION, e);
			publishProgress(SPEC.EVENT.ERROR);
		}

		return card;
	}
}
