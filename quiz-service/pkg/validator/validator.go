package validator

import (
	"fmt"
	"reflect"
	"strings"
)

// Validate performs validation on a struct using struct tags
// It supports basic required field validation and can be extended for other validations
func Validate(s interface{}) error {
	v := reflect.ValueOf(s)

	// If pointer, get the underlying element
	if v.Kind() == reflect.Ptr {
		v = v.Elem()
	}

	// Only accept structs
	if v.Kind() != reflect.Struct {
		return fmt.Errorf("validation only works on structs; got %T", s)
	}

	t := v.Type()
	for i := 0; i < v.NumField(); i++ {
		field := t.Field(i)
		value := v.Field(i)

		// Check 'validate' tag
		tag := field.Tag.Get("validate")
		if tag == "" {
			continue
		}

		// Process each validation rule
		rules := strings.Split(tag, ",")
		for _, rule := range rules {
			// Required field validation
			if rule == "required" {
				if isEmptyValue(value) {
					return fmt.Errorf("field '%s' is required but was empty", field.Name)
				}
			}
		}
	}

	return nil
}

// isEmptyValue checks if a value is considered empty
func isEmptyValue(v reflect.Value) bool {
	switch v.Kind() {
	case reflect.String:
		return v.Len() == 0
	case reflect.Bool:
		return !v.Bool()
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
		return v.Int() == 0
	case reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64:
		return v.Uint() == 0
	case reflect.Float32, reflect.Float64:
		return v.Float() == 0
	case reflect.Slice, reflect.Map:
		return v.Len() == 0
	case reflect.Interface, reflect.Ptr:
		return v.IsNil()
	}
	return false
}
