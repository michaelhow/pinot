package com.linkedin.pinot.core.operator.query;

import com.linkedin.pinot.common.request.AggregationInfo;
import com.linkedin.pinot.common.request.GroupBy;
import com.linkedin.pinot.core.block.query.ProjectionBlock;
import com.linkedin.pinot.core.common.Block;
import com.linkedin.pinot.core.common.BlockId;
import com.linkedin.pinot.core.common.Operator;
import com.linkedin.pinot.core.query.aggregation.groupby.GroupByConstants;


/**
 * The most generic GroupBy Operator which will take all the required dataSources
 * and do aggregation and groupBy.
 * 
 * @author xiafu
 *
 */
public class MAggregationFunctionGroupByOperator extends AggregationFunctionGroupByOperator {

  public MAggregationFunctionGroupByOperator(AggregationInfo aggregationInfo, GroupBy groupBy,
      Operator projectionOperator) {
    super(aggregationInfo, groupBy, projectionOperator);
  }

  @Override
  public Block nextBlock() {
    ProjectionBlock block = (ProjectionBlock) _projectionOperator.nextBlock();
    if (block != null) {
      for (int i = 0; i < _groupBy.getColumnsSize(); ++i) {
        _groupByBlockValIterators[i] = block.getBlock(_groupBy.getColumns().get(i)).getBlockValueSet().iterator();
      }
      for (int i = 0; i < _aggregationColumns.length; ++i) {
        _aggregationFunctionBlockValIterators[i] = block.getBlock(_aggregationColumns[i]).getBlockValueSet().iterator();
      }
    }
    while (_groupByBlockValIterators[0].hasNext()) {
      String groupKey = getGroupKey();
      _aggregateGroupedValue.put(groupKey,
          _aggregationFunction.aggregate(_aggregateGroupedValue.get(groupKey), _aggregationFunctionBlockValIterators));
    }
    return null;
  }

  private String getGroupKey() {
    String groupKey = _groupByBlockValIterators[0].nextStringVal();
    for (int i = 1; i < _groupByBlockValIterators.length; ++i) {
      groupKey +=
          (GroupByConstants.GroupByDelimiter.groupByMultiDelimeter + _groupByBlockValIterators[i].nextStringVal());
    }
    return groupKey;
  }

  @Override
  public Block nextBlock(BlockId BlockId) {
    throw new UnsupportedOperationException(
        "Method: nextBlock(BlockId BlockId) is Not Supported in MAggregationFunctionGroupByOperator");
  }

}