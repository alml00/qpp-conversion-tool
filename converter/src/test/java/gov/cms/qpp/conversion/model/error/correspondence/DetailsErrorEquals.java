package gov.cms.qpp.conversion.model.error.correspondence;

import java.util.Objects;

import com.google.common.truth.Correspondence;

import gov.cms.qpp.conversion.model.error.Detail;
import gov.cms.qpp.conversion.model.error.LocalizedError;

public final class DetailsErrorEquals extends Correspondence<Detail, LocalizedError> {

	private DetailsErrorEquals() {
	}

	public static DetailsErrorEquals INSTANCE = new DetailsErrorEquals();

	@Override
	public boolean compare(Detail actual, LocalizedError expected) {
		if (actual == null) {
			return expected == null;
		}
		return Objects.equals(actual.getMessage(), expected.getMessage()) &&
				actual.getErrorCode() == expected.getErrorCode();
	}

	@Override
	public String toString() {
		return null;
	}
}
