{
  package: "org.bpmscript",
  class: 
  [
    {
      name: "Person",
      id: {name:id},
      property: [
        { name: "firstName", not_null: true, type: "string", length=255}
        { name: "surname", not_null: true, type: "string", length=255}
        { name: "age", not_null: true, type: "date"}
      ],
      many_to_one: [
        { name: "company", class: "Company" }
      ]
    },
    {
      name: "Company",
      id: {name:id},
      property: [
        { name: "name", not_null: true, type: "string", length=255}
      ],
      set: [
        { name: "employees", class: "Person" }
      ]
    }
  ]
}



before:
    {
      name: "Person",
      id: {name:id},
      property: [
        { name: "firstName", not_null: true, type: "string", length=255}
        { name: "surname", not_null: true, type: "string", length=255}
        { name: "age", not_null: true, type: "date"}
      ],
      many_to_one: [
        { name: "company", class: "Company" }
      ]
    },

after:

function Person(orm) {
    this.id = orm.id({generator:"uuid-hex"})
    this.firstName = orm.string({length:"", column:"FIRST_NAME", nullable: false}),
    this.surName = orm.string({nullable: false}),
    this.age = orm.date({nullable: false}),
    this.company = orm.manyToOne({class:"Company"}),
    this.children = orm.set({sdfasdf}))
}
