# TODO

* KstructNode's values are immutable, but KstructMap's children and attributes aren't. Should everything be immutable?
  Or maybe everything mutable?
* If everything's immutable, we need a KstructNode::setIn(value, vararg path) that creates new wrappers
* If we implemented KstructNode::setIn, we also need a KstructNode::getIn(vararg path)
