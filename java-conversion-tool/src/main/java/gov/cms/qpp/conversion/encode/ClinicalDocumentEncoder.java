package gov.cms.qpp.conversion.encode;

import gov.cms.qpp.conversion.decode.ClinicalDocumentDecoder;
import gov.cms.qpp.conversion.decode.MultipleTinsDecoder;
import gov.cms.qpp.conversion.model.Encoder;
import gov.cms.qpp.conversion.model.Node;
import gov.cms.qpp.conversion.model.TemplateId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Encoder to serialize the root node of the Document-Level Template: QRDA Category III Report (ClinicalDocument).
 */

@Encoder(TemplateId.CLINICAL_DOCUMENT)
public class ClinicalDocumentEncoder extends QppOutputEncoder {
	static final String PERFORMANCE_END = "performanceEnd";
	static final String PERFORMANCE_YEAR = "performanceYear";
	static final String PERFORMANCE_START = "performanceStart";
	private static final String MEASUREMENT_SETS = "measurementSets";

	/**
	 * internalEncode encodes nodes into Json Wrapper.
	 *
	 * @param wrapper will hold the json format of nodes
	 * @param thisNode holds the decoded node sections of clinical document
	 */
	@Override
	public void internalEncode(JsonWrapper wrapper, Node thisNode) {
		encodeToplevel(wrapper, thisNode);
		encodeEntityId(wrapper, thisNode);
		Map<TemplateId, Node> childMapByTemplateId = thisNode.getChildNodes().stream().collect(
				Collectors.toMap(Node::getType, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));

		JsonWrapper measurementSets =
			encodeMeasurementSets(childMapByTemplateId);
			wrapper.putObject(MEASUREMENT_SETS, measurementSets);
	}

	/**
	 * This will add the attributes from the Clinical Document Node
	 *
	 * @param wrapper will hold the json format of nodes
	 * @param thisNode holds the decoded node sections of clinical document
	 */
	private void encodeToplevel(JsonWrapper wrapper, Node thisNode) {
		wrapper.putString(ClinicalDocumentDecoder.PROGRAM_NAME,
				thisNode.getValue(ClinicalDocumentDecoder.PROGRAM_NAME));
		wrapper.putString(ClinicalDocumentDecoder.ENTITY_TYPE,
				thisNode.getValue(ClinicalDocumentDecoder.ENTITY_TYPE));
		wrapper.putString(MultipleTinsDecoder.TAX_PAYER_IDENTIFICATION_NUMBER,
				thisNode.getValue(MultipleTinsDecoder.TAX_PAYER_IDENTIFICATION_NUMBER));
		wrapper.putString(MultipleTinsDecoder.NATIONAL_PROVIDER_IDENTIFIER,
				thisNode.getValue(MultipleTinsDecoder.NATIONAL_PROVIDER_IDENTIFIER));
	}

	/**
	 * This will add the entityId from the Clinical Document Node
	 *
	 * @param wrapper will hold the json format of nodes
	 * @param thisNode holds the decoded node sections of clinical document
	 */
	private void encodeEntityId(JsonWrapper wrapper, Node thisNode) {
		String entityId = thisNode.getValue(ClinicalDocumentDecoder.ENTITY_ID);
		if (entityId != null && !entityId.isEmpty()) {
			wrapper.putString(ClinicalDocumentDecoder.ENTITY_ID, entityId);
		}
	}

	/**
	 * Method for encoding each child measurement set
	 *
	 * @param childMapByTemplateId object that represents the document's children
	 * @return encoded measurement sets
	 */
	private JsonWrapper encodeMeasurementSets(Map<TemplateId, Node> childMapByTemplateId) {
		JsonWrapper measurementSetsWrapper = new JsonWrapper();
		JsonWrapper childWrapper;
		JsonOutputEncoder sectionEncoder;

		for (Node child : childMapByTemplateId.values()) {
			if (TemplateId.NPI_TIN_ID == child.getType()) {
				continue; //MultiTINS is not a real encoder.
			}
			childWrapper = new JsonWrapper();
			sectionEncoder = ENCODERS.get(child.getType());
			try {
				sectionEncoder.encode(childWrapper, child);
				measurementSetsWrapper.putObject(childWrapper);
			} catch (NullPointerException exc) {
				String message = "No encoder for decoder : " + child.getType();
				throw new EncodeException(message, exc);
			}
		}
		return measurementSetsWrapper;
	}
}
