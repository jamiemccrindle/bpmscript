function HibernateObjectToRelationalMapper() {}
HibernateObjectToRelationalMapper.prototype = {
  id:function(params) {
    return function(name) {
      return <id name={name}></id>;
    }
  },
  string:function(params) {
    return function(name) {
      return <property name={name} type="string"/>
    }
  },
  date:function(params) {
      return <property name={name} type="date"/>
  },
  manyToOne:function(params) {
      return <many-to-one name={name}/>
  },
  set:function(params) {
      return <set name={name}/>
  },
}

function getHibernateClassMapping(object) {
  var result = new XMLList();
  for (var key in object) {
    var value = object[key];
    result += value(key);
  }
  return <class name={object.getClassName()}></class>
}

function Person(orm) {
    this.id = orm.id({generator:"uuid-hex"})
    this.firstName = orm.string({length:"", column:"FIRST_NAME", nullable: false}),
    this.surName = orm.string({nullable: false}),
    this.age = orm.date({nullable: false}),
    this.company = orm.manyToOne({class:"Company"}),
    this.children = orm.set({sdfasdf}))
}
